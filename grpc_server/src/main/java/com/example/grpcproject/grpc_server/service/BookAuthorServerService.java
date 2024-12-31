package com.example.grpcproject.grpc_server.service;

import com.example.grpcproject.grpc_server.service.tempdb.TempDB;
import com.grpcproject.Author;
import com.grpcproject.Book;
import com.grpcproject.BookAuthorServiceGrpc;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.ArrayList;
import java.util.List;

@GrpcService
public class BookAuthorServerService extends BookAuthorServiceGrpc.BookAuthorServiceImplBase {

    @Override
    public void getAuthor(Author request, StreamObserver<Author> responseObserver) {
        TempDB.getAuthorsFromTempDb()
                .stream()
                .filter(author -> author.getAuthorId() == request.getAuthorId())
                .findFirst()
                .ifPresent(responseObserver::onNext);
        responseObserver.onCompleted();
    }

    @Override
    public void getBookByAuthor(Author request, StreamObserver<Book> responseObserver) {
        TempDB.getBooksFromTempDb()
                .stream()
                .filter(book -> book.getAuthorId() == request.getAuthorId())
                .forEach(responseObserver::onNext);
        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<Book> getExpensiveBook(StreamObserver<Book> responseObserver) {
        return new StreamObserver<Book>() {
            @Override
            public void onNext(Book book) {
                System.err.println("[Server] Unexpected input from client.");
            }
            @Override
            public void onError(Throwable throwable) {
                System.err.println("[Server] Error occurred: " + throwable.getMessage());
                responseObserver.onError(throwable);
            }
            @Override
            public void onCompleted() {
                System.out.println("[Server] Fetching books from TempDB...");
                List<Book> books = TempDB.getBooksFromTempDb();

                Book expensiveBook = books.stream()
                        .filter(book -> book.getPrice() > 500.0)
                        .findFirst().orElse(null);

                responseObserver.onNext(expensiveBook);
                responseObserver.onCompleted();
                System.out.println("[Server] Stream completed.");
            }
        };
    }


    @Override
    public StreamObserver<Book> getBooksByGender(StreamObserver<Book> responseObserver) {
        return new StreamObserver<Book>() {
            final List<Book> bookList = new ArrayList<>();

            @Override
            public void onNext(Book book) {
                String gender = book.getTitle();
                TempDB.getAuthorsFromTempDb()
                        .stream()
                        .filter(author -> author.getGender().equalsIgnoreCase(gender))
                        .forEach(author -> {
                            TempDB.getBooksFromTempDb()
                                    .stream()
                                    .filter(bookFromDb -> bookFromDb.getAuthorId() == author.getAuthorId())
                                    .forEach(bookList::add);
                        });
            }

            @Override
            public void onError(Throwable throwable) {
                responseObserver.onError(throwable);
            }

            @Override
            public void onCompleted() {
                bookList.forEach(responseObserver::onNext);
                responseObserver.onCompleted();
            }
        };
    }

}
