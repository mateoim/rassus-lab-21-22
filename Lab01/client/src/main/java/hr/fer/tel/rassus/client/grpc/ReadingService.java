package hr.fer.tel.rassus.client.grpc;

import hr.fer.tel.rassus.client.Client;
import hr.fer.tel.rassus.client.model.Reading;
import hr.fer.tel.rassus.examples.ReadingGrpc;
import hr.fer.tel.rassus.examples.ReadingRequest;
import hr.fer.tel.rassus.examples.ReadingResponse;
import io.grpc.stub.StreamObserver;

import java.util.logging.Logger;

public class ReadingService extends ReadingGrpc.ReadingImplBase {

    private static final Logger logger = Logger.getLogger(ReadingService.class.getName());

    private final Client client;

    public ReadingService(Client client) {
        this.client = client;
    }

    @Override
    public void requestReading(ReadingRequest request, StreamObserver<ReadingResponse> responseObserver) {
        logger.info("New request form " + request.getId());

        Reading reading = client.getLatestReading();
        ReadingResponse response = ReadingResponse.newBuilder()
                .setTemperature(reading.getTemperature())
                .setPressure(reading.getPressure())
                .setHumidity(reading.getHumidity())
                .setCo(reading.getCo())
                .setNo2(reading.getNo2())
                .setSo2(reading.getSo2())
                .build();

        responseObserver.onNext(response);
        logger.info("Response sent");
        responseObserver.onCompleted();
    }
}
