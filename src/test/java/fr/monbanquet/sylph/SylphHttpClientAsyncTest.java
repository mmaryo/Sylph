/*
 * MIT License
 *
 * Copyright (c) 2019 Monbanquet.fr
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package fr.monbanquet.sylph;

import fr.monbanquet.sylph.parser.DefaultParser;
import fr.monbanquet.sylph.parser.Parser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

public class SylphHttpClientAsyncTest {

    private static final String TODOS_URL = "http://jsonplaceholder.typicode.com/todos";
    private static final String TODO_1_URL = TODOS_URL + "/1";

    private static final Parser parser = DefaultParser.create();

    @Test
    void standard_java_http_client() {
        // given
        HttpRequest request = HttpRequest.newBuilder()
                .header("Content-Type", "application/json; charset=utf-8")
                .uri(URI.create(TODO_1_URL))
                .GET()
                .copy()
                .version(HttpClient.Version.HTTP_2)
                .timeout(Duration.ofSeconds(5))
                .build();
        HttpClient client = HttpClient.newHttpClient();

        // when
        HttpResponse<String> response = client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .join();

        // then
        String body = response.body();
        Todo todo = parser.deserialize(body, Todo.class);
        AssertTodo.assertResult(todo);
    }

    @Test
    void as_standard_java_http_client() throws IOException {
        // given
        SylphHttpRequest request = SylphHttpRequest.newBuilder()
                .header("Content-Type", "application/json; charset=utf-8")
                .uri(URI.create(TODO_1_URL))
                .GET()
                .copy()
                .version(HttpClient.Version.HTTP_2)
                .timeout(Duration.ofSeconds(5))
                .build();
        SylphHttpClient client = SylphHttpClient.newHttpClient();

        // when
        HttpResponse<String> response = client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .join();

        // then
        String body = response.body();
        Todo todo = parser.deserialize(body, Todo.class);
        AssertTodo.assertResult(todo);
    }

    @Test
    void minimal_builder() {
        // when
        Todo todo = Sylph.newClient()
                .GET(TODO_1_URL)
                .sendAsync(Todo.class)
                .join()
                .asObject();

        // then
        AssertTodo.assertResult(todo);
    }

    @Test
    void minimal_builder_shortcut() {
        // when
        Todo todo = Sylph.newClient()
                .GET(TODO_1_URL)
                .bodyAsync(Todo.class)
                .join();

        // then
        AssertTodo.assertResult(todo);
    }

    @Test
    void minimal_builder_list() {
        // when
        List<Todo> todos = Sylph.newClient()
                .GET(TODOS_URL)
                .sendAsync(Todo.class)
                .join()
                .asList();

        // then
        AssertTodo.assertResult(todos);
    }

    @Test
    void minimal_builder_list_shortcut() {
        // when
        List<Todo> todos = Sylph.newClient()
                .GET(TODOS_URL)
                .bodyListAsync(Todo.class)
                .join();

        // then
        AssertTodo.assertResult(todos);
    }

    @Test
    void all_methods_builder() {
        // given
        SylphHttpClient http = Sylph.builder()
                .setBaseRequest(SylphHttpRequest.newBuilder()
                        .uri(TODO_1_URL)
                        .copy()
                        .GET()
                        .version(HttpClient.Version.HTTP_2))
                .setClient(SylphHttpClient.newBuilder()
                        .priority(1)
                        .version(HttpClient.Version.HTTP_2)
                        .followRedirects(HttpClient.Redirect.ALWAYS)
                )
                .setParser(DefaultParser.create())
                .getClient();

        // when
        Todo responseBody = http.sendAsync(Todo.class)
                .join()
                .asObject();

        // then
        AssertTodo.assertResult(responseBody);
    }

    @Test
    void post() {
        // given
        Todo todo = Helper.newTodoExtended();

        Parser p = DefaultParser.create();

        String s = p.serialize(todo);

        // when
        Todo todoResult = Sylph.newClient()
                .POST(TODOS_URL, todo)
                .sendAsync(Todo.class)
                .join()
                .asObject();

        // then
        Assertions.assertNotEquals(todoResult.getId(), todo.getId());
    }

    @Test
    void put() {
        // given
        Todo todo = Helper.newTodo();

        // when
        Todo todoResult = Sylph.newClient()
                .PUT(TODOS_URL + "/" + todo.getId(), todo)
                .sendAsync(Todo.class)
                .join()
                .asObject();

        // then
        Assertions.assertEquals(todoResult.getId(), todo.getId());
    }

    @Test
    void delete() {
        // when
        Todo todoResult = Sylph.newClient()
                .DELETE(TODOS_URL + "/44")
                .sendAsync(Todo.class)
                .join()
                .asObject();

        // then
        Assertions.assertEquals(todoResult.getId(), 0);
    }

}

