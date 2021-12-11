import socket
import random
import threading
import time


class SimpleSimulatedDatagramSocket(socket.socket):
    def __init__(self, loss_rate: float, average_delay: int):
        super().__init__(socket.AF_INET, socket.SOCK_DGRAM)

        self.loss_rate = loss_rate
        self.average_delay = average_delay

        super().settimeout(4 * average_delay)

    def send_packet(self, packet, address):
        if random.uniform(0, 1) >= self.loss_rate:
            sleep_time = 2 * self.average_delay * random.uniform(0, 1)
            threading.Thread(target=self._send_packet, args=(packet, address, sleep_time)).start()

    def _send_packet(self, packet, address, sleep_time):
        time.sleep(sleep_time / 1000)
        super().sendto(packet, address)
