class Reading:
    def __init__(self, temperature, pressure, humidity, co=None, no2=None, so2=None):
        self.temperature = temperature
        self.pressure = pressure
        self.humidity = humidity
        self.co = co
        self.no2 = no2
        self.so2 = so2
