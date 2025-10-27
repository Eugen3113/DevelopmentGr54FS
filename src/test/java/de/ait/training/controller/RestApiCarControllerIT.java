package de.ait.training.controller;

import de.ait.training.model.Car;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class RestApiCarControllerIT {

   // используем RANDOM_PORT
    @LocalServerPort
    private int port;

    //делаем реальные Http: запросы
    @Autowired
    TestRestTemplate restTemplate;

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    @Test
    @DisplayName("price between 10000 and 30000, 3 cars were found, status OK")
    @Sql(scripts = {"classpath:sql/clear.sql", "classpath:sql/seed_cars.sql"})
    void testPriceBetween10000And30000() throws Exception {
        ResponseEntity<Car[]> response = restTemplate.getForEntity(url("/api/cars/price/between/10000/30000"),
                Car[].class);
        //assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<Car> cars = Arrays.asList(response.getBody());
        assertThat(cars.size()).isEqualTo(3);
        assertThat(cars.get(0).getModel()).isEqualTo("BMW x5");

    }

    @Test
    @DisplayName("price under 16000, 1 car was found, status OK")
    @Sql(scripts = {"classpath:sql/clear.sql", "classpath:sql/seed_cars.sql"})
    void testPriceUnder16000Success() {
        ResponseEntity<Car[]> response = restTemplate.getForEntity(url("/api/cars/price/under/16000"),
                Car[].class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<Car> cars = Arrays.asList(response.getBody());
        assertThat(cars.size()).isEqualTo(1);
        assertThat(cars.get(0).getModel()).isEqualTo("Audi A4");
    }

    @Test
    @DisplayName("wrong min and max price, 0 cars ware found, status BadRequest")
    void testMinMaxPricesWrongFail() {
        ResponseEntity<Car[]> response = restTemplate.getForEntity(url("/api/cars/price/between/30000/10000"),
                Car[].class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        Car[] result = response.getBody();
        List<Car> cars = Arrays.asList(response.getBody());
        assertThat(cars.isEmpty()).isEqualTo(true);
    }

    //==============================================================================================================
            //1. возвращает список всех автомобилей из базы данных.
            //✅ Данные есть:
    @Test
    @DisplayName("should return all cars when data exists, status OK")
    @Sql(scripts = {"classpath:sql/clear.sql", "classpath:sql/seed_cars.sql"})
    void shouldReturnAllCarsWhenDataExists() {
        ResponseEntity<Car[]> response = restTemplate.getForEntity(url("/api/cars"), Car[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        List<Car> cars = Arrays.asList(response.getBody());
        assertThat(cars.size()).isEqualTo(4); // если в seed_cars.sql 4 записи
    }
          //⚙️ Данных нет:
    @Test
    @DisplayName("should return empty list when no cars,status OK ")
    //@Sql(scripts = {"classpath:sql/clear.sql", "classpath:sql/seed_cars.sql"}) // надо убрать "classpath:sql/seed_cars.sql"(типа данных нет)
     @Sql(scripts = {"classpath:sql/clear.sql"}) // - правильно
    void shouldReturnEmptyListWhenNoCars() {
        ResponseEntity<Car[]> response = restTemplate.getForEntity(url("/api/cars"), Car[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        List<Car> cars = Arrays.asList(response.getBody());
        assertThat(cars.isEmpty());

    }

    //==============================0
         //2. фильтрует автомобили по цвету, регистр букв не имеет значения.
         //✅ Цвет найден (регистр не важен):
    @Test
    @DisplayName("should return cars with color red regardless of case, status OK")
    @Sql(scripts = {"classpath:sql/clear.sql", "classpath:sql/seed_cars.sql"})
    void shouldReturnCarsWithColorRedIgnoringCase() {
        ResponseEntity<Car[]> response = restTemplate.getForEntity(url("/api/cars/color/ReD"), Car[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        List<Car> cars = Arrays.asList(response.getBody());
        assertThat(cars.isEmpty()).isEqualTo(false);
        assertThat(cars.stream().allMatch(car -> car.getColor().equalsIgnoreCase("red"))).isTrue();
    }
         //⚙️ Цвет не найден:
    @Test
    @DisplayName("should return 404 when color not found,status 404 NOT_FOUND ")
    @Sql(scripts = {"classpath:sql/clear.sql", "classpath:sql/seed_cars.sql"})
    void shouldReturn404WhenColorNotFound() {
        ResponseEntity<Car[]> response = restTemplate.getForEntity(url("/api/cars/color/purple"), Car[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();

        List<Car> cars = Arrays.asList(response.getBody());
        assertThat(cars.isEmpty());
    }

    //===============================================================


    //3. возвращает автомобили, у которых цена находится в заданном диапазоне (включительно).
         // ✅ Автомобили найдены:

    @Test
    @DisplayName("price between 10000 and 30000, 3 cars were found, status OK")
    @Sql(scripts = {"classpath:sql/clear.sql", "classpath:sql/seed_cars.sql"})
    void shouldReturnPriceBetween10000And30000() {
        ResponseEntity<Car[]> response = restTemplate.getForEntity(url("/api/cars/price/between/10000/30000"), Car[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        List<Car> cars = Arrays.asList(response.getBody());
        assertThat(cars.size()).isEqualTo(3);

    }

        // ❌ Некорректный диапазон (max < min):

    @Test
    @DisplayName("price between 30000 and 10000, 0 cars ware found, status BadRequest")
    @Sql(scripts = {"classpath:sql/clear.sql", "classpath:sql/seed_cars.sql"})
    void shouldReturnPriceBetween30000And10000() {
        ResponseEntity<Car[]> response = restTemplate.getForEntity(url("/api/cars/price/between/30000/10000"), Car[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        Car[] result = response.getBody();
        List<Car> cars = Arrays.asList(response.getBody());
        assertThat(cars.isEmpty()).isEqualTo(true);

    }


         //⚙️ Нет подходящих по цене:
    @Test
    @DisplayName("price between 100 and 500, 0 cars ware found, status 404 NOT_FOUND")
    @Sql(scripts = {"classpath:sql/clear.sql", "classpath:sql/seed_cars.sql"})
    void shouldReturnPriceBetween100And500() {
        ResponseEntity<Car[]> response = restTemplate.getForEntity(url("/api/cars/price/between/100/500"), Car[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();

        List<Car> cars = Arrays.asList(response.getBody());
        assertThat(cars.isEmpty());

    }


    //=====================================

    //4: возвращает автомобили, у которых цена меньше или равна max.
           //✅ Есть подходящие автомобили:

    @Test
    @DisplayName("price under 20000, 2 car was found, status OK")
    @Sql(scripts = {"classpath:sql/clear.sql", "classpath:sql/seed_cars.sql"})
    void shouldReturnPriceUnder20000Success() {
        ResponseEntity<Car[]> response = restTemplate.getForEntity(url("/api/cars/price/under/20000"),
                Car[].class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<Car> cars = Arrays.asList(response.getBody());
        assertThat(cars.size()).isEqualTo(2);

    }

         // ⚙️ Нет таких:
    @Test
    @DisplayName("price under 1000, 0 car was found, status 404 NOT_FOUND")
    @Sql(scripts = {"classpath:sql/clear.sql", "classpath:sql/seed_cars.sql"})
    void shouldReturnPriceUnder1000Success() {
        ResponseEntity<Car[]> response = restTemplate.getForEntity(url("/api/cars/price/under/1000"),
                Car[].class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        List<Car> cars = Arrays.asList(response.getBody());
        assertThat(cars.isEmpty());

    }

    //======================================
    //5. возвращает автомобили, у которых цена больше или равна min.
    //✅ Есть подходящие автомобили:

    @Test
    @DisplayName("price over 25000, 2 car was found, status OK")
    @Sql(scripts = {"classpath:sql/clear.sql", "classpath:sql/seed_cars.sql"})
    void shouldReturnPriceOver25000Success() {
        ResponseEntity<Car[]> response = restTemplate.getForEntity(url("/api/cars/price/over/25000"),
                Car[].class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<Car> cars = Arrays.asList(response.getBody());
        assertThat(cars.size()).isEqualTo(2);
        assertThat(cars.get(0).getModel()).isEqualTo("BMW x5", "Ferrari" );

    }
          //⚙️ Нет таких:
    @Test
    @DisplayName("price over 1000000,0 cars ware found, status 404 NOT_FOUND")
    @Sql(scripts = {"classpath:sql/clear.sql", "classpath:sql/seed_cars.sql"})
    void shouldReturnPriceOver1000000Success() {
        ResponseEntity<Car[]> response = restTemplate.getForEntity(url("/api/cars/price/over/1000000"),
                Car[].class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        List<Car> cars = Arrays.asList(response.getBody());
        assertThat(cars.isEmpty());

    }

}

