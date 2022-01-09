import time
import random


class EmulatedSystemClock:
    def __init__(self):
        self.start_time = round(time.time() * 1000)
        self.jitter = random.randint(0, 20) / 100

    def current_time_millis(self):
        current = round(time.time() * 1000)
        diff = current - self.start_time
        coef = diff / 1000
        return self.start_time + round(diff * ((1 + self.jitter) ** coef))

    def update_time_millis(self, seed):
        return max(self.current_time_millis(), seed) + 1
