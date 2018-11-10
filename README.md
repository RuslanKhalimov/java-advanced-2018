# Курс «Технологии Java» ИТМО

## Домашнее задание 10. HelloUDP
Условие:

 1. Реализуйте клиент и сервер, взаимодействующие по *UDP*.
 2. Класс `HelloUDPClient` должен отправлять запросы на сервер, принимать результаты и выводить их на консоль.
    * Аргументы командной строки:
        1. имя или ip-адрес компьютера, на котором запущен сервер;
        2. номер порта, на который отсылать запросы;
        3. префикс запросов (строка);
        4. число параллельных потоков запросов;
        5. число запросов в каждом потоке.
    * Запросы должны одновременно отсылаться в указанном числе потоков. Каждый поток должен ожидать обработки своего запроса и выводить сам запрос и результат его обработки на консоль. Если запрос не был обработан, требуется послать его заного.
    * Запросы должны формироваться по схеме *<префикс запросов><номер потока>_<номер запроса в потоке>*.
 3. Класс `HelloUDPServer` должен принимать задания, отсылаемые классом `HelloUDPClient` и отвечать на них.
    * Аргументы командной строки:
        1. номер порта, по которому будут приниматься запросы;
        2. число рабочих потоков, которые будут обрабатывать запросы.
    * Ответом на запрос должно быть *Hello, <текст запроса>*.
    * Если сервер не успевает обрабатывать запросы, прием запросов может быть временно приостановлен.
 4. *Бонусный вариант*. Реализация должна быть полностью неблокирующей.
 Клиент не должен создавать потоков.
 В реализации не должно быть активных ожиданий, в том числе через `Selector`.

Решение:
   * [сложный вариант](java/ru/ifmo/rain/khalimov/hello)
     
Исходный код тестов:
   * [интерфейсы и вспомогательные классы](java/info/kgeorgiy/java/advanced/hello/)
   * [Клиент](java/info/kgeorgiy/java/advanced/hello/HelloClientTest.java)
   * [Сервер](java/info/kgeorgiy/java/advanced/hello/HelloServerTest.java)
   
Запуск тестов: 
   * простой вариант:
       - клиент: `info.kgeorgiy.java.advanced.hello.Tester client <полное имя класса>`
       - сервер: `info.kgeorgiy.java.advanced.hello.Tester server <полное имя класса>`
   * сложный вариант:
       - клиент: `info.kgeorgiy.java.advanced.hello.Tester client-i18n <полное имя класса>`
       - сервер: `info.kgeorgiy.java.advanced.hello.Tester server-i18n <полное имя класса>`

## Домашнее задание 9. Web Crawler
Условие:

 Напишите потокобезопасный класс `WebCrawler`, который будет рекурсивно обходить сайты.
 
 1. Класс `WebCrawler` должен иметь конструктор
 
     ```java
     public WebCrawler(Downloader downloader, int downloaders, int extractors, int perHost)
     ```
    * `downloader` позволяет скачивать страницы и извлекать из них ссылки;
    * `downloaders` — максимальное число одновременно загружаемых страниц;
    * `extractors` — максимальное число страниц, из которых извлекаются ссылки;
    * `perHost` — максимальное число страниц, одновременно загружаемых c одного хоста. Для опредения хоста следует использовать метод getHost класса URLUtils из тестов.
 
 2. Класс `WebCrawler` должен реализовывать интерфейс `Crawler`
     ```java
     public interface Crawler extends AutoCloseable {   
        List<String> download(String url, int depth) throws IOException;
        void close();
     }
     ```
    * Метод `download` должен рекурсивно обходить страницы, начиная с указанного `URL` на указанную глубину и возвращать список загруженных страниц и файлов.
         - Например, если глубина равна 1, то должна быть загружена только указанная страница.
         - Если глубина равна 2, то указанная страница и те страницы и файлы, на которые она ссылается и так далее.
         - Этот метод может вызываться параллельно в нескольких потоках.
    * Загрузка и обработка страниц (извлечение ссылок) должна выполняться максимально параллельно, с учетом ограничений на число одновременно загружаемых страниц (в том числе с одного хоста) и страниц, с которых загружаются ссылки.
    * Для распараллеливания разрешается создать до *downloaders + extractors* вспомогательных потоков.
    * Загружать и/или извлекать ссылки из одной и той же страницы запрещается1.
    * Метод `close` должен завершать все вспомогательные потоки.
 
 3. Для загрузки страниц должен применяться `Downloader`, передаваемый первым аргументом конструктора.
     ```java
     public interface Downloader {
 	    public Document download(final String url) throws IOException;
     }
     ```
     * Метод `download` загружает документ по его адресу (`URL`).
     * Документ позволяет получить ссылки по загруженной странице:
     ```java
     public interface Document {
         List<String> extractLinks() throws IOException;
     }
     ```
     * Ссылки, возвращаемые документом являются абсолютными и имеют схему `http` или `https`.
 4. Должен быть реализован метод `main`, позволяющий запустить обход из командной строки
     * Командная строка
         > WebCrawler url [downloads [extractors [perHost]]]
 
     * Для загрузки страниц требуется использовать реализацию CachingDownloader из тестов.
Решение:
    * [простой вариант](java/ru/ifmo/rain/khalimov/crawler)
     
Исходный код тестов:
    * [интерфейсы и вспомогательные классы](java/info/kgeorgiy/java/advanced/crawler/)
    * [простой вариант](java/info/kgeorgiy/java/advanced/crawler/CrawlerEasyTest.java)
    * [сложный вариант](java/info/kgeorgiy/java/advanced/crawler/CrawlerHardTest.java)

Запуск тестов: 
   * простой вариант: `info.kgeorgiy.java.advanced.crawler.Tester easy <полное имя класса>`
   * сложный вариант: `info.kgeorgiy.java.advanced.crawler.Tester hard <полное имя класса>`

## Домашнее задание 8. Параллельный запуск
Условие:
  1. Напишите класс `ParallelMapperImpl`, реализующий интерфейс `ParallelMapper`.
     ```java 
     public interface ParallelMapper extends AutoCloseable {
         <T, R> List<R> run(
             Function<? super T, ? extends R> f, 
             List<? extends T> args
         ) throws InterruptedException;
     
         @Override
         void close() throws InterruptedException;
     }
     ```
     * Метод run должен параллельно вычислять функцию *f* на каждом из указанных аргументов (*args*).
     * Метод `close` должен останавливать все рабочие потоки.
     * Конструктор `ParallelMapperImpl(int threads)` создает `threads` рабочих потоков, 
       которые могут быть использованы для распараллеливания.
     * К одному `ParallelMapperImpl` могут одновременно обращаться несколько клиентов.
     * Задания на исполнение должны накапливаться в очереди и обрабатываться в порядке поступления.
       В реализации не должно быть активных ожиданий.
  2. Модифицируйте касс `IterativeParallelism` так, чтобы он мог использовать `ParallelMapper`.
     * Добавьте конструктор `IterativeParallelism(ParallelMapper)`
     * Методы класса должны делить работу на `threads` фрагментов и исполнять их при помощи `ParallelMapper`.
     * Должна быть возможность одновременного запуска и работы нескольких клиентов, использующих один `ParallelMapper`.
     * При наличии `ParallelMapper` сам `IterativeParallelism` новые потоки создавать не должен.

Решение:
* [сложный вариант](java/ru/ifmo/rain/khalimov/mapper)

Исходный код тестов:

* [простой вариант](java/info/kgeorgiy/java/advanced/mapper/ScalarMapperTest.java)
* [сложный вариант](java/info/kgeorgiy/java/advanced/mapper/ListMapperTest.java)

Запуск тестов: 
   * простой вариант: `info.kgeorgiy.java.advanced.mapper.Tester scalar <ParallelMapperImpl>,<IterativeParallelism>`
   * сложный вариант: `info.kgeorgiy.java.advanced.mapper.Tester list <ParallelMapperImpl>,<IterativeParallelism>`

## Домашнее задание 7. Итеративный параллелизм

Условие:
  1. Реализуйте класс `IterativeParallelism`, который будет обрабатывать списки в несколько потоков.
  2. В простом варианте должны быть реализованы следующие методы:
     * `minimum(threads, list, comparator)` — первый минимум;
     * `maximum(threads, list, comparator)` — первый максимум;
     * `all(threads, list, predicate)` — проверка, что все элементы списка удовлетворяют предикату;
     * `any(threads, list, predicate)` — проверка, что существует элемент списка, удовлетворяющий предикату.
  3. В сложном варианте должны быть дополнительно реализованы следующие методы:
     * `filter(threads, list, predicate)` — вернуть список, содержащий элементы удовлетворяющие предикату;
     * `map(threads, list, function)` — вернуть список, содержащий результаты применения функции;
     * `join(threads, list)` — конкатенация строковых представлений элементов списка.
  4. Во все функции передается параметр `threads` — сколько потоков надо использовать при вычислении. Вы можете рассчитывать, что число потоков не велико.
  5. Не следует рассчитывать на то, что переданные компараторы, предикаты и функции работают быстро.
  6. При выполнении задания нельзя использовать *Concurrency Utilities*.

Решение:
* [сложный вариант](java/ru/ifmo/rain/khalimov/concurrent)

Интерфейсы:
* простой вариант класс должен реализовывать интерфейс [ScalarIP](java/info/kgeorgiy/java/advanced/concurrent/ScalarIP.java).
* сложный вариант класс должен реализовывать интерфейс [ListIP](java/info/kgeorgiy/java/advanced/concurrent/ListIP.java).

Исходный код тестов:

* [простой вариант](java/info/kgeorgiy/java/advanced/concurrent/ScalarIPTest.java)
* [сложный вариант](java/info/kgeorgiy/java/advanced/concurrent/ListIPTest.java)

Запуск тестов: 
   * простой вариант: `info.kgeorgiy.java.advanced.concurrent.Tester scalar <полное имя класса>`
   * сложный вариант: `info.kgeorgiy.java.advanced.concurrent.Tester list <полное имя класса>`

## Домашнее задание 6. Javadoc

Условие:
  1. Документируйте класс `Implementor` и сопутствующие классы с применением **Javadoc**.
     * Должны быть документированы все классы и все члены классов, в том числе закрытые (`private`).
     * Документация должна генерироваться без предупреждений.
     * Сгенерированная документация должна содержать корректные ссылки на классы стандартной библиотеки.
  2. Для проверки, кроме исходного кода так же должны быть предъявлены:
     * скрипт для генерации документации;
     * сгенерированная документация.

Генерация **Javadoc**:
  * `generateJavaDoc.bat`

## Домашнее задание 5. JarImplementor

Условие:
  1. Создайте *.jar*-файл, содержащий скомпилированный `Implementor` и сопутствующие классы.
     * Созданный *.jar*-файл должен запускаться командой java `-jar`.
     * Запускаемый *.jar*-файл должен принимать те же аргументы командной строки, что и класс `Implementor`.
  2. Модифицируйте `Implemetor` так, что бы при запуске с аргументами `-jar имя-класса файл.jar` он генерировал *.jar*-файл 
     с реализацией соответствующего класса (интерфейса).
  3. Для проверки, кроме исходного кода так же должны быть предъявлены:
     * скрипт для создания запускаемого *.jar*-файла, в том числе, исходный код манифеста;
     * запускаемый *.jar*-файл.

Генерация *.jar* файла:
  * `generateImplementorJar.bat`

Пример запуска:
  * `java -jar Implementor.jar java.util.Set`
  * `java -jar Implementor.jar -jar java.util.Set Set.jar`
  

Класс должен реализовывать интерфейс
[JarImpler](java/info/kgeorgiy/java/advanced/implementor/JarImpler.java).

Решение:
* [простой вариант](java/ru/ifmo/rain/khalimov/implementor)

Исходный код тестов:

* [простой вариант](java/info/kgeorgiy/java/advanced/implementor/InterfaceJarImplementorTest.java)
* [сложный вариант](java/info/kgeorgiy/java/advanced/implementor/ClassJarImplementorTest.java)

Запуск тестов: 
   * простой вариант: `info.kgeorgiy.java.advanced.implementor.Tester jar-interface <полное имя класса>`
   * сложный вариант: `info.kgeorgiy.java.advanced.implementor.Tester jar-class <полное имя класса>`

## Домашнее задание 4. Implementor

Условие:
  1. Реализуйте класс `Implementor`, который будет генерировать реализации классов и интерфейсов.
     * Аргументы командной строки: полное имя класса/интерфейса, для которого требуется сгенерировать реализацию.
     * В результате работы должен быть сгенерирован java-код класса с суффиксом *Impl*, расширяющий (реализующий) указанный класс (интерфейс).
     * Сгенерированный класс должен компилироваться без ошибок.
     * Сгенерированный класс не должен быть абстрактным.
     * Методы сгенерированного класса должны игнорировать свои аргументы и возвращать значения по умолчанию.
  2. В задании выделяются три уровня сложности:
     *  *Простой* — `Implementor` должен уметь реализовывать только интерфейсы (но не классы). Поддержка *generics* не требуется.
     *  *Сложный* — `Implementor` должен уметь реализовывать и классы и интерфейсы. Поддержка *generics* не требуется.
     *  *Бонусный* — `Implementor` должен уметь реализовывать *generic*-классы и интерфейсы. Сгенерированный код должен иметь корректные параметры типов и не порождать *UncheckedWarning*.

Решение:
* [простой вариант](java/ru/ifmo/rain/khalimov/implementor)

Класс должен реализовывать интерфейс
[Impler](java/info/kgeorgiy/java/advanced/implementor/Impler.java).

Исходный код тестов:

* [простой вариант](java/info/kgeorgiy/java/advanced/implementor/InterfaceImplementorTest.java)
* [сложный вариант](java/info/kgeorgiy/java/advanced/implementor/ClassImplementorTest.java)

Запуск тестов: 
   * простой вариант: `info.kgeorgiy.java.advanced.implementor.Tester interface <полное имя класса>`
   * сложный вариант: `info.kgeorgiy.java.advanced.implementor.Tester class <полное имя класса>`

## Домашнее задание 3. Студенты

Условие:
  1. Разработайте класс `StudentDB`, осуществляющий поиск по базе данных студентов.
     * Класс `StudentDB` должен реализовывать интерфейс `StudentQuery` (простая версия) или `StudentGroupQuery` (сложная версия).
     * Каждый методы должен состоять из ровного одного оператора. При этом длинные операторы надо разбивать на несколько строк.
  2. При выполнении задания следует обратить внимание на:
     * Применение лямбда-выражений и поток.
     * Избавление от повторяющегося кода.

Решение:
  * [простой вариант](java/ru/ifmo/rain/khalimov/student)

Исходный код

 * простой вариант:
    [интерфейс](java/info/kgeorgiy/java/advanced/student/StudentQuery.java),
    [тесты](java/info/kgeorgiy/java/advanced/student/StudentQueryFullTest.java)
 * сложный вариант:
    [интерфейс](java/info/kgeorgiy/java/advanced/student/StudentGroupQuery.java),
    [тесты](java/info/kgeorgiy/java/advanced/student/StudentGroupQueryFullTest.java)

Запуск тестов: 
   * простой вариант: `info.kgeorgiy.java.advanced.student.Tester StudentQuery <полное имя класса>`
   * сложный вариант: `info.kgeorgiy.java.advanced.student.Tester StudentGroupQuery <полное имя класса>`

## Домашнее задание 2. ArraySortedSet

Условие:
  1. Разработайте класс `ArraySet`, реализующие неизменяемое упорядоченное множество.
     * Класс `ArraySet` должен реализовывать интерфейс `SortedSet` (упрощенная версия) или `NavigableSet` (усложненная версия).
     * Все операции над множествами должны производиться с максимально возможной асимптотической эффективностью.
  2. При выполнении задания следует обратить внимание на:
     * Применение стандартных коллекций.
     *  Избавление от повторяющегося кода.

Решение:
 * [сложный вариант](java/ru/ifmo/rain/khalimov/arrayset)

Исходный код тестов:

 * [простой вариант](java/info/kgeorgiy/java/advanced/arrayset/SortedSetTest.java)
 * [сложный вариант](java/info/kgeorgiy/java/advanced/arrayset/NavigableSetTest.java)

Запуск тестов: 
   * простой вариант: `info.kgeorgiy.java.advanced.arrayset.Tester SortedSet <полное имя класса>`
   * сложный вариант: `info.kgeorgiy.java.advanced.arrayset.Tester NavigableSet <полное имя класса>`

## Домашнее задание 1. Обход файлов

Условие:

  1. Разработайте класс Walk, осуществляющий подсчет хеш-сумм файлов.
     * Формат запуска `java Walk <входной файл> <выходной файл>`
     * Входной файл содержит список файлов, которые требуется обойти.
     * Выходной файл должен содержать по одной строке для каждого файла. Формат строки:
         `<шестнадцатеричная хеш-сумма> <путь к файлу>`
     * Для подсчета хеш-суммы используйте алгоритм [FNV](https://ru.wikipedia.org/wiki/FNV).
     * Если при чтении файла возникают ошибки, укажите в качестве его хеш-суммы 00000000.
     * Кодировка входного и выходного файлов — `UTF-8`.
     * Размеры файлов могут превышать размер оперативной памяти.
  2. Усложненная версия:
     * Разработайте класс RecursiveWalk, осуществляющий подсчет хеш-сумм файлов в директориях
     * Входной файл содержит список файлов и директорий, которые требуется обойти.
       Обход директорий осуществляется рекурсивно.
  3. При выполнении задания следует обратить внимание на:
     * Дизайн и обработку исключений, диагностику ошибок.
     * Программа должна корректно завершаться даже в случае ошибки.
     * Корректная работа с вводом-выводом.
     * Отсутствие утечки ресурсов.

Решение:
 * [простой вариант](java/ru/ifmo/rain/khalimov/walk)


Исходный код тестов:

 * [простой вариант](java/info/kgeorgiy/java/advanced/walk/WalkTest.java)
 * [сложный вариант](java/info/kgeorgiy/java/advanced/walk/RecursiveWalkTest.java)

Запуск тестов: 
   * простой вариант: `info.kgeorgiy.java.advanced.walk.Tester Walk <полное имя класса>`
   * сложный вариант: `info.kgeorgiy.java.advanced.walk.Tester RecursiveWalk <полное имя класса>`
