import socket
import random
import threading
import time


class SimpleSimulatedDatagramSocket(socket.socket):
    def __init__(self, port: int, loss_rate: float, average_delay: int):
        super().__init__(socket.AF_INET, socket.SOCK_DGRAM)

        self.address = ('localhost', port)
        self.loss_rate = loss_rate
        self.average_delay = average_delay

        super().settimeout(0)

    def send_packet(self, packet):
        if random.uniform(0, 1) >= self.loss_rate:
            sleep_time = 2 * self.average_delay * random.uniform(0, 1)
            threading.Thread(target=self._send_packet, args=(packet, sleep_time)).start()

    def _send_packet(self, packet, sleep_time):
        time.sleep(sleep_time)
        super().sendto(packet, self.address)
