import json
import pickle
import socket
import threading
import time

import kafka

from emulated_system_clock import EmulatedSystemClock
from reading import Reading
from simple_simulated_datagram_socket import SimpleSimulatedDatagramSocket
from time_vector import TimeVector

readings = '../readings[2].csv'
loss_rate = 0.3
average_delay = 1000
buffer_size = 1024
max_retransmissions = 10


class Node:
    def __init__(self):
        self.id = None
        self.port = None
        self.clock = EmulatedSystemClock()
        self.consumer = None
        self.producer = None
        self.local_readings = []
        self.all_readings = set()
        self.running = True
        self.waiting_for_ack = dict()
        self.sent_ack = set()
        self.got_ack = set()
        self.connections = dict()
        self.client_socket = SimpleSimulatedDatagramSocket(loss_rate, average_delay)
        self.vector_time = None
        self.scalar_time = 0
        self.lines = None
        self.counter = 0

    def run(self):
        self.start()
        self.register()

        threading.Thread(target=self._run_listener).start()

        self.main_loop()

        print(f'SORTED BY SCALAR TIME: {sorted(self.all_readings, key=lambda x: x[1])}')
        print(f'SORTED BY VECTOR TIME: {sorted(self.all_readings, key=lambda x: x[2])}')

    def start(self):
        self.id = int(input('Please enter node id: '))
        self.port = int(input('Please enter node port: '))
        self.vector_time = TimeVector(self.id)

        while len(self.vector_time) < self.id:
            self.vector_time.append(0)

        self.consumer = kafka.KafkaConsumer('Command', 'Register', bootstrap_servers='localhost:9092')

        waiting = True

        while waiting:
            start_command = self.consumer.poll()

            if start_command:
                for key, value in start_command.items():
                    if key.topic == 'Command':
                        for element in value:
                            if element.value == b'Start':
                                waiting = False
                    elif key.topic == 'Registration':
                        for element in value:
                            self._register(element)

    def register(self):
        registration_dict = {'id': self.id, 'address': 'localhost', 'port': self.port}
        self.producer = kafka.KafkaProducer(bootstrap_servers=['localhost:9092'])

        self.producer.send('Register', json.dumps(registration_dict).encode('utf-8'))

    def main_loop(self):
        recent = []
        local_keys = set(self.connections.keys())
        with open(readings) as f:
            self.lines = f.readlines()

        while self.running:
            time.sleep(1)

            # update time
            while self.sent_ack:
                message = self.sent_ack.pop()
                if message not in self.all_readings:
                    recent.append(message)
                    self.all_readings.add(message)

                for i in range(min(len(self.vector_time), len(message[2]))):
                    self.vector_time[i] = max(self.vector_time[i], message[2][i])
                self.vector_time[self.id - 1] += 1
                self.scalar_time = self.clock.update_time_millis(message[1])

            # check received acks
            while self.got_ack:
                node_id, reading_id = self.got_ack.pop()
                to_remove = None

                for element in self.waiting_for_ack[node_id]:
                    if element[0] == reading_id:
                        to_remove = element
                        break

                if to_remove is not None:
                    self.vector_time[self.id - 1] += 1
                    self.scalar_time = self.clock.update_time_millis(self.scalar_time)
                    self.waiting_for_ack[node_id].remove(to_remove)

            # generate new reading and send it
            self.generate_readings()
            reading = self.local_readings[-1]
            recent.append(reading)

            local_keys = set(self.connections.keys()) if len(local_keys) != len(self.connections.keys()) else local_keys

            for node_id in local_keys:
                print(f'Sending node {node_id} reading {reading}.')
                binary_message = pickle.dumps(reading)
                self.client_socket.send_packet(binary_message, self.connections[node_id])
                self.waiting_for_ack[node_id].add((reading[3].id, binary_message))

            self.counter += 1

            # send retransmissions if enough time has passed
            if self.counter % 2 == 0:
                for node_id in local_keys:
                    counter = 0
                    for message in self.waiting_for_ack[node_id]:
                        if counter > max_retransmissions:
                            break

                        print(f'Retransmission to node {node_id} reading {message[0]}.')
                        self.client_socket.send_packet(message[1], self.connections[node_id])
                        counter += 1

            if self.counter % 5 == 0:
                print(f'SORTED BY SCALAR TIME: {sorted(recent, key=lambda x: x[1])}')
                print(f'SORTED BY VECTOR TIME: {sorted(recent, key=lambda x: x[2])}')

                total, size = 0, 0

                for value in recent:
                    size += 1
                    total += value[3].co

                print(f'AVERAGE IN LAST 5 SECONDS: {total / size}')
                recent.clear()

    def generate_readings(self):
        current_time = self.clock.current_time_millis() // 1000
        line = self.lines[((self.clock.start_time - current_time) % 100) + 1]
        parts = line.strip().split(',')
        reading = Reading(self.counter, parts[3])
        self.vector_time[self.id - 1] += 1
        self.scalar_time = self.clock.update_time_millis(self.scalar_time)
        reading_tuple = (self.id, self.scalar_time, self.vector_time.copy(), reading)
        self.local_readings.append(reading_tuple)
        self.all_readings.add(reading_tuple)
        print(f'Generated reading {reading}')

    def _poll_consumer(self):
        message = None

        while message != {}:
            message = self.consumer.poll()

            if message:
                for key, value in message.items():
                    if key.topic == 'Command':
                        for element in value:
                            if element.value == b'Stop':
                                self.running = False
                    elif key.topic == 'Register':
                        for element in value:
                            self._register(element)

    def _run_listener(self):
        server_socket = SimpleSimulatedDatagramSocket(loss_rate, average_delay)
        server_socket.bind(('localhost', self.port))
        server_socket.settimeout(1)

        while self.running:
            self._poll_consumer()

            if not self.running:
                break

            try:
                data = server_socket.recvfrom(buffer_size)
                decoded_message = pickle.loads(data[0])
                print(f'Received message {decoded_message}')
                if len(decoded_message) == 4:
                    self.sent_ack.add(decoded_message)
                    ack = (self.id, decoded_message[3].id)
                    print(f'Sending ack {ack} to node {decoded_message[0]}.')
                    address = self.connections.get(decoded_message[0])
                    if address is not None:
                        self.client_socket.send_packet(pickle.dumps(ack), address)
                elif len(decoded_message) == 2:
                    self.got_ack.add(decoded_message)
            except socket.timeout:
                continue

    def _register(self, element):
        registration = json.loads(element.value.decode('utf-8'))
        node_id = registration['id']
        if self.id != node_id:
            while len(self.vector_time) < node_id:
                self.vector_time.append(0)
            self.waiting_for_ack[node_id] =\
                set((reading[3].id, pickle.dumps(reading)) for reading in self.local_readings)
            self.connections[node_id] = (registration['address'], registration['port'])


if __name__ == '__main__':
    node = Node()
    node.run()
