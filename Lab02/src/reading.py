class Reading:
    def __init__(self, temperature, pressure, humidity, co=None, no2=None, so2=None):
        self.temperature = temperature
        self.pressure = pressure
        self.humidity = humidity
        self.co = co
        self.no2 = no2
        self.so2 = so2

    def __repr__(self) -> str:
        return f'{self.temperature},{self.pressure},{self.humidity},{self.co},{self.no2},{self.so2}'
