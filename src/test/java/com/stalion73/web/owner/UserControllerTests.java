package com.stalion73.web.owner;

import java.util.Random;

import com.stalion73.model.User;
import com.stalion73.Stalion73Application;
import com.stalion73.model.Consumer;
import com.stalion73.model.Supplier;


import static org.assertj.core.api.Assertions.assertThat;
import static io.restassured.RestAssured.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static io.restassured.RestAssured.given;

import org.springframework.http.HttpHeaders;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.mediatype.problem.Problem;
import org.testng.annotations.Test;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class UserControllerTests {

    HttpHeaders headers = new HttpHeaders();
    
    @Test
    public void login(){
        User user = new User();
        user.setUsername("rodri");
        user.setPassword("1234");

        User usr =  given()
        .basePath("/users/login")
        .port(8080)
        .contentType("application/json")
        .body(user)
        .when()
        .post().prettyPeek()
        .then()
        .statusCode(200)
        .extract().body().as(User.class);
        
        String token = usr.getToken();
        this.headers.add("Authorization", token);
        assertThat(token).contains("Bearer");
    }

    @Test
    public void loginIneffectedId(){
        User user = new User();
        user.setUsername("non-exist");
        user.setPassword("1234");

        Problem p =  given()
        .basePath("/users/login")
        .port(8080)
        .contentType("application/json")
        .body(user)
        .when()
        .post().prettyPeek()
        .then()
        .statusCode(404)
        .extract().body().as(Problem.class);
        
        assertEquals("Usuario incorrecto", p.getTitle());
        assertEquals("El usuario no existe.", p.getDetail());
        
    }

    @Test
    public void logout(){
        User user = new User();
        user.setUsername("rodri");
        user.setPassword("1234");

        User usr =  given()
        .basePath("/users/login")
        .port(8080)
        .contentType("application/json")
        .body(user)
        .when()
        .post().prettyPeek()
        .then()
        .statusCode(200)
        .extract().body().as(User.class);
        
        String token = usr.getToken();
        this.headers.add("Authorization", token);

        usr = given().queryParam("username", "rodri")
        .basePath("/users/logout")
        .port(8080)
        .contentType("application/json")
        .headers(this.headers)
        .when()
        .post().prettyPeek()
        .then()
        .statusCode(200)
        .extract().body().as(User.class);

        assertThat(usr.isEnabled()).isEqualTo(false);
    }

    @Test
    public void profile(){
        User user = new User();
        user.setUsername("rodri");
        user.setPassword("1234");

        User usr =  given()
        .basePath("/users/login")
        .port(8080)
        .contentType("application/json")
        .body(user)
        .when()
        .post().prettyPeek()
        .then()
        .statusCode(200)
        .extract().body().as(User.class);
        
        String token = usr.getToken();
        this.headers.add("Authorization", token);

       given()
        .basePath("/users/profile")
        .port(8080)
        .contentType("application/json")
        .headers(headers)
        .when()
        .get().prettyPeek()
        .then()
        .statusCode(200);
        
    }

    @Test
    public void signup(){
        Supplier supplier = new Supplier();
        Random rand = new Random();
        supplier.setName("name_" + Integer.valueOf(rand.nextInt()));
        supplier.setLastname("lastname_" + + Integer.valueOf(rand.nextInt()));
        supplier.setDni("00000000A");
        supplier.setEmail(Integer.valueOf(rand.nextInt()) + "@gmail.com");
        User user = new User();
        user.setUsername("new_username_" + Integer.valueOf(rand.nextInt()));
        user.setPassword("1234");
        supplier.setUser(user);

        given()
        .basePath("/users/signup/suppliers")
        .port(8080)
        .contentType("application/json")
        .body(supplier)
        .when()
        .post().prettyPeek()
        .then()
        .statusCode(201);

    }

    @Test
    public void signupBindingErrors(){
        Supplier supplier = new Supplier();
        Random rand = new Random();
        supplier.setName("");
        supplier.setLastname("");
        supplier.setDni("00000000A");
        supplier.setEmail(Integer.valueOf(rand.nextInt()) + "@gmail.com");
        User user = new User();
        user.setUsername("new_username_" + Integer.valueOf(rand.nextInt()));
        user.setPassword("1234");
        supplier.setUser(user);

        Problem p = given()
        .basePath("/users/signup/suppliers")
        .port(8080)
        .contentType("application/json")
        .body(supplier)
        .when()
        .post().prettyPeek()
        .then()
        .statusCode(400)
        .extract().body().as(Problem.class);


        assertEquals("Error de validación", p.getTitle());
        assertEquals("El usuario no se ha podido validadar correctamente.", p.getDetail());
 
    }

    @Test
    public void signupAlreadyExistingUser(){
        Supplier supplier = new Supplier();
        Random rand = new Random();
        supplier.setName("name_" + Integer.valueOf(rand.nextInt()));
        supplier.setLastname("lastname_" + + Integer.valueOf(rand.nextInt()));
        supplier.setDni("00000000A");
        supplier.setEmail(Integer.valueOf(rand.nextInt()) + "@gmail.com");
        User user = new User();
        user.setUsername("rodri");
        user.setPassword("1234");
        supplier.setUser(user);

        Problem p = given()
        .basePath("/users/signup/suppliers")
        .port(8080)
        .contentType("application/json")
        .body(supplier)
        .when()
        .post().prettyPeek()
        .then()
        .statusCode(400)
        .extract().body().as(Problem.class);

        assertEquals("El usuario ya existe", p.getTitle());
        assertEquals("El nombre de usuario que usted a puesto ya existe en base de datos. Por favor, elija otro.", p.getDetail());


    }


}