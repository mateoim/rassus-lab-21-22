import json
import pickle
import socket
import threading
import time

import kafka

from emulated_system_clock import EmulatedSystemClock
from reading import Reading
from simple_simulated_datagram_socket import SimpleSimulatedDatagramSocket

readings = '../readings[2].csv'
loss_rate = 0.3
average_delay = 1000
buffer_size = 1024


class Node:
    def __init__(self):
        self.id = None
        self.port = None
        self.clock = EmulatedSystemClock()
        self.registrations = []
        self.consumer = None
        self.producer = None
        self.local_readings = []
        self.all_readings = []
        self.running = True
        self.waiting_for_ack = dict()
        self.send_ack = set()
        self.connections = dict()
        self.vector_time = []

    def run(self):
        self.start()
        self.register()

        threading.Thread(target=self._run_listener).start()
        threading.Thread(target=self.run_server).start()

        self.generate_readings()

        print(self.all_readings)

    def start(self):
        self.id = int(input('Please enter node id: '))
        self.port = int(input('Please enter node port: '))
        self.vector_time = [0 for _ in range(self.id)]

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

    def run_server(self):
        current_index = -1

        while self.running:
            self._poll_consumer()
            final_index = len(self.local_readings) - 1

            if current_index < final_index:
                for reading in self.local_readings[current_index + 1:]:
                    for node_id, connection in self.connections.items():
                        print(f'Sending node {node_id} reading {reading}.')
                        connection.send_packet(pickle.dumps(reading))
                        self.waiting_for_ack[node_id].add(reading)

            current_index = final_index

            while self.send_ack:
                message = self.send_ack.pop()
                ack = (self.id, message[1], message[2])
                for i in range(min(len(self.vector_time), len(message[2]))):
                    if i == self.id:
                        continue
                    self.vector_time[i] = max(self.vector_time[i], message[2][i])
                print(f'Sending ack {ack} to node {message[0]}.')
                self.connections[message[0]].send_packet(pickle.dumps(ack))

            for node_id, connection in self.connections.items():
                for reading in self.waiting_for_ack[node_id]:
                    print(f'Sending node {node_id} reading {reading}.')
                    connection.send_packet(pickle.dumps(reading))
                    break

    def generate_readings(self):
        with open(readings) as f:
            lines = f.readlines()

        while self.running:
            current_time = self.clock.current_time_millis() // 1000
            line = lines[((self.clock.start_time - current_time) % 100) + 1]
            parts = line.strip().split(',')
            reading = Reading(parts[0], parts[1], parts[2], parts[3], parts[4], parts[5])
            self.vector_time[self.id - 1] += 1
            reading_tuple = (self.id, current_time, tuple(self.vector_time), reading)
            self.local_readings.append(reading_tuple)
            self.all_readings.append(reading_tuple)
            print(f'Generated reading {reading}')
            time.sleep(1)

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
        print('listener started')
        server_socket = SimpleSimulatedDatagramSocket(self.port, loss_rate, average_delay)
        server_socket.bind(('localhost', self.port))
        server_socket.settimeout(1)

        while self.running:
            try:
                data = server_socket.recvfrom(buffer_size)
                decoded_message = pickle.loads(data[0])
                print(f'Received message {decoded_message}')
                if len(decoded_message) == 4:
                    self.all_readings.append(decoded_message)
                    self.send_ack.add(decoded_message[:-1])
                elif len(decoded_message) == 3:
                    self.waiting_for_ack[decoded_message[0]].remove(decoded_message[1:])
            except socket.timeout:
                continue

    def _register(self, element):
        registration = json.loads(element.value.decode('utf-8'))
        node_id = registration['id']
        if self.id != node_id:
            while len(self.vector_time) < node_id:
                self.vector_time.append(0)
            self.registrations.append(registration)
            self.waiting_for_ack[node_id] = set(self.local_readings)
            self.connections[node_id] = SimpleSimulatedDatagramSocket(registration['port'], loss_rate, average_delay)


if __name__ == '__main__':
    node = Node()
    node.run()
