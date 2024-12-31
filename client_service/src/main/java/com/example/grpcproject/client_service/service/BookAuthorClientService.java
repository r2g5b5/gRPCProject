package com.example.grpcproject.client_service.service;

import com.grpcproject.Author;
import com.grpcproject.Book;
import com.grpcproject.BookAuthorServiceGrpc;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Service
public class BookAuthorClientService {

    @GrpcClient("grpc-project-service")
    BookAuthorServiceGrpc.BookAuthorServiceBlockingStub syncClient;

    @GrpcClient("grpc-project-service")
    BookAuthorServiceGrpc.BookAuthorServiceStub asyncClient;

    public Map<String, Object> getAuthor(int id) {
        Author authorRequest = Author.newBuilder().setAuthorId(id).build();
        Author authorResponse = syncClient.getAuthor(authorRequest);
        Map<String, Object> authorFields = new HashMap<>();
        authorResponse.getAllFields().forEach((field, value) -> {
            authorFields.put(field.getName(), value);
        });
        return authorFields;
    }

    public List<Map<String, Object>> getBooks(int id) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        Author authorRequest = Author.newBuilder().setAuthorId(id).build();
        final List<Map<String, Object>> response = new ArrayList<>();

        asyncClient.getBookByAuthor(authorRequest, new StreamObserver<Book>() {
            @Override
            public void onNext(Book book) {
                Map<String, Object> bookFields = new HashMap<>();
                book.getAllFields().forEach((field, value) -> {
                    bookFields.put(field.getName(), value);
                });
                response.add(bookFields);
            }

            @Override
            public void onError(Throwable throwable) {
                latch.countDown();
            }

            @Override
            public void onCompleted() {
                latch.countDown();
            }
        });

        boolean await = latch.await(1, TimeUnit.MINUTES);
        return await ? response : Collections.emptyList();
    }

    public Map<String, List<Map<String, Object>>> getExpensiveBook() throws InterruptedException {
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        final Map<String, List<Map<String, Object>>> response = new HashMap<>();
        final List<Map<String, Object>> expensiveBooksList = new ArrayList<>();

        System.out.println("Initializing gRPC call...");

        StreamObserver<Book> requestObserver = asyncClient.getExpensiveBook(new StreamObserver<Book>() {
            @Override
            public void onNext(Book book) {
                System.out.println("Received book: " + book.getTitle() + ", price: " + book.getPrice());

                Map<String, Object> bookFields = new HashMap<>();
                book.getAllFields().forEach((field, value) -> {
                    bookFields.put(field.getName(), value);
                });
                expensiveBooksList.add(bookFields);
            }

            @Override
            public void onError(Throwable throwable) {
                System.err.println("Error received from server: " + throwable.getMessage());
                countDownLatch.countDown();
            }

            @Override
            public void onCompleted() {
                System.out.println("Server completed the stream.");
                response.put("Expensive Book", expensiveBooksList);
                countDownLatch.countDown();
            }
        });

        System.out.println("Starting to fetch expensive book...");

        requestObserver.onCompleted();

        boolean await = countDownLatch.await(1, TimeUnit.MINUTES);
        if (!await) {
            System.err.println("Timeout while waiting for server response.");
        }

        return await ? response : Collections.emptyMap();
    }


    public List<Map<String, Object>> getBooksByGender(String gender) throws InterruptedException {
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        final List<Map<String, Object>> response = new ArrayList<>();
        StreamObserver<Book> responseObserver = asyncClient.getBooksByGender(new StreamObserver<Book>() {
            @Override
            public void onNext(Book book) {
                Map<String, Object> bookFields = new HashMap<>();
                book.getAllFields().forEach((field, value) -> {
                    String fieldName = field.getName();
                    bookFields.put(fieldName, value);
                });
                response.add(bookFields);
            }

            @Override
            public void onError(Throwable throwable) {
                countDownLatch.countDown();
            }

            @Override
            public void onCompleted() {
                countDownLatch.countDown();
            }
        });

        responseObserver.onNext(Book.newBuilder().setAuthorId(0).setTitle(gender).build());
        responseObserver.onCompleted();

        boolean await = countDownLatch.await(1, TimeUnit.MINUTES);
        return await ? response : Collections.emptyList();
    }


}
