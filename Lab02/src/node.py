import kafka
import json


class Node:
    def __init__(self):
        self.id = None
        self.port = None
        self.registrations_set = set()
        self.consumer = None
        self.producer = None

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
                    elif key == 'Registration':
                        for element in value:
                            self.registrations_set.add(element)

        print('starting')


if __name__ == '__main__':
    node = Node()
    node.start()
