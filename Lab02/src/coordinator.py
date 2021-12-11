import kafka


if __name__ == '__main__':
    coordinator = kafka.KafkaProducer(bootstrap_servers=['localhost:9092'])

    line = ''
    while line.lower() != 'start':
        line = input('Type "start" to send Start message:\n')

    coordinator.send('Command', b'Start')

    while line.lower() != 'stop':
        line = input('Type "stop" to send Stop message:\n')

    coordinator.send('Command', b'Stop')
    coordinator.flush()
    coordinator.close()
