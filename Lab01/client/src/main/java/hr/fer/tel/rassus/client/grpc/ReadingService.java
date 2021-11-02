package hr.fer.tel.rassus.client.grpc;

import hr.fer.tel.rassus.examples.ReadingGrpc;
import hr.fer.tel.rassus.examples.ReadingRequest;
import hr.fer.tel.rassus.examples.ReadingResponse;
import io.grpc.stub.StreamObserver;

import java.util.logging.Logger;

public class ReadingService extends ReadingGrpc.ReadingImplBase {

    private static final Logger logger = Logger.getLogger(ReadingService.class.getName());

    @Override
    public void requestReading(ReadingRequest request, StreamObserver<ReadingResponse> responseObserver) {
        super.requestReading(request, responseObserver);
    }
}
