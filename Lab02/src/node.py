import kafka
import json
import pickle
import threading

from emulated_system_clock import EmulatedSystemClock
from simple_simulated_datagram_socket import SimpleSimulatedDatagramSocket


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
        self.connections = dict()

    def run(self):
        self.start()
        self.register()

        threading.Thread(target=self.run_server()).start()

        while self.running:
            pass

        print(self.registrations)

    def start(self):
        self.id = input('Please enter node id: ')
        self.port = input('Please enter node port: ')

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

            # TODO receive readings and retransmission

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

    def _register(self, element):
        registration = json.loads(element.value.decode('utf-8'))
        if self.id != registration['id']:
            self.registrations.append(registration)
            self.waiting_for_ack[registration['id']] = set(self.local_readings)
            self.connections[registration['id']] = SimpleSimulatedDatagramSocket(registration['port'], 0.3, 1000)


if __name__ == '__main__':
    node = Node()
    node.run()
