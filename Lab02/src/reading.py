class Reading:
    def __init__(self, temperature, pressure, humidity, co='', no2='', so2=''):
        self.temperature = float(temperature)
        self.pressure = float(pressure)
        self.humidity = float(humidity)
        self.co = float(co) if co != '' else 0
        self.no2 = float(no2) if no2 != '' else 0
        self.so2 = float(so2) if so2 != '' else 0

    def __repr__(self) -> str:
        return f'{self.temperature},{self.pressure},{self.humidity},{self.co},{self.no2},{self.so2}'
