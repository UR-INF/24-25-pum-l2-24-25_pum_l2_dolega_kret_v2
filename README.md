<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> 21cd4525f62bc0410ec836db8b005cf7e279bd8c
Programowanie urządzeń mobilnych laboratorium L_2_ 

# Dokumentacja projetu: **Aplikacja mobilna wspomagająca zarządzanie pojazdami**

## Zespoł projetowy:
_Alicja Dołęga
Aleksander Kret_

## Opis projektu
Projekt opiera się na wielofunkcyjnej aplikacji mobilnej dedykowanej kierowcom, mającej na celu usprawnienie zarządzania pojazdami oraz zwiększenie wygody codziennego użytkowania samochodu. Aplikacja umożliwia m.in. obliczanie zużycia paliwa na podstawie wprowadzonych danych (lub parametrów GPS), dodawanie samochodów z pełną specyfikacją techniczną, monitorowanie przeglądów technicznych z funkcją powiadomień o zbliżających się terminach, a także lokalizowanie najbliższych stacji benzynowych przy użyciu GPS. Dodatkowo, aplikacja oferuje opcję udostępniania lokalizacji oraz trasy innym użytkownikom.

## Zakres projektu opis funkcjonalności:
1. Dodawanie i zarządzanie pojazdami:

- Użytkownik może dodawać pojazdy do aplikacji, podając szczegółowe dane techniczne, takie jak: marka, model, rocznik, pojemność silnika, rodzaj paliwa i spalanie.
- Możliwość edycji danych pojazdu w razie potrzeby.

2. Obliczanie zużycia paliwa:

- Funkcja ręcznego wprowadzania danych o przejechanych kilometrach i zatankowanym paliwie w celu obliczenia średniego spalania.
- Integracja z GPS: aplikacja może automatycznie śledzić trasę użytkownika i na tej podstawie obliczać orientacyjne zużycie paliwa.

3. Monitorowanie przeglądów technicznych:

- Możliwość zapisania daty ostatniego przeglądu technicznego.
- Powiadomienia push przypominające użytkownikowi o zbliżającym się przeglądzie.

4. Lokalizacja najbliższych stacji benzynowych:

- Integracja z Google Maps by umożliwić wytyczenie trasy do najbliższej stacji benzynowej.

5. Udostępnianie lokalizacji i tras:

- Funkcja pozwalająca na udostępnianie bieżącej lokalizacji znajomym lub rodzinie.
- Możliwość przesłania pełnej trasy przejazdu (z obliczonym czasem dojazdu) przez SMS.

6. Historia tras i zużycia paliwa:

- Automatyczne zapisywanie historii przejechanych tras, zużycia paliwa oraz wizyt na stacjach benzynowych.
- Możliwość przeglądania raportów miesięcznych/rocznych.

## Panele / zakładki aplikacji 
- Panel logowania

![image](https://github.com/user-attachments/assets/ce13ef39-1ad5-444b-935b-0b87de2177be)

- Strona główna

![image](https://github.com/user-attachments/assets/6dc984e0-6fe8-424e-8589-4988469897ac)

- Pojazdy

![image](https://github.com/user-attachments/assets/531e329d-2415-4d6e-8726-588b3a4d07e2)

- Informacja o danym pojeździe

![image](https://github.com/user-attachments/assets/76b453a0-fa2c-4676-86ea-146b12791ac2)

- Edytowanie danego pojazdu

![image](https://github.com/user-attachments/assets/3c1ae838-4003-422d-9b0b-892e4901c52b)

- Dodawanie pojazdu

![image](https://github.com/user-attachments/assets/5bc5c18c-44a3-41ac-81e8-2d72a7cbc5df)

- Wybieranie trasy danym pojazdem i obliczanie zużytego paliwa/dystansu

![image](https://github.com/user-attachments/assets/535c63bd-e7fd-4c75-a90c-433afa80d7df)

- Historia tras

![image](https://github.com/user-attachments/assets/9aaa8844-0405-4d2b-aa62-a122dfae659b)

## Baza danych
###### Diagram ERD
![PUMDiagramERD](https://github.com/user-attachments/assets/31a952af-f4f2-4ab2-ada9-ae88edbec593)

###### Opis bazy danych
1. Tabela users (tabela użytkowników aplikacji):  
- userId: Unikalny identyfikator użytkownika (klucz główny).
- email: Adres e-mail użytkownika.
password: Hasło użytkownika.
2. Tabela trips (tabela tras dla danego pojazdu i użytkownika):  
- tripId: Unikalny identyfikator podróży (klucz główny).
- startLocation: Miejsce początkowe podróży.
- destination: Miejsce docelowe podróży.
- distance: Dystans podróży w kilometrach.
- fuelUsed: Zużycie paliwa podczas podróży w litrach.
- date: Data podróży.
- carName: Nazwa samochodu używanego podczas podróży.
- userId: Identyfikator użytkownika (klucz obcy odnoszący się do users.userId).
- vehicleId: Identyfikator pojazdu (klucz obcy odnoszący się do vehicles.vehicleId).
3. Tabela vehicles (tabela pojazdów danego użytkownika):
- vehicleId: Unikalny identyfikator pojazdu (klucz główny).
- brand: Marka pojazdu.
- model: Model pojazdu.
- year: Rok produkcji pojazdu.
- fuelType: Rodzaj paliwa używanego przez pojazd.
- engineCapacity: Pojemność silnika pojazdu w litrach.
- fuelConsumption: Zużycie paliwa przez pojazd na 100 km.
- lastService: Data ostatniego serwisu pojazdu.
- userId: Identyfikator użytkownika (klucz obcy odnoszący się do users.userId).
###### Relacje:
Jeden użytkownik (users) może mieć wiele podróży (trips).
Jeden użytkownik (users) może mieć wiele pojazdów (vehicles).
Każda podróż (trips) jest powiązana z jednym użytkownikiem (users) i jednym pojazdem (vehicles).

## Wykorzystane uprawnienia aplikacji do:
- Internetu
- Lokalizacji
- Powiadomień

## Dane potrzebne do konfiguracji podczas pierwszego uruchomienia (jeśli wymagane)
Przykładowe dane logowania:
- login: test@test.pl
- hasło: 123456
<<<<<<< HEAD
=======
"# my_project" 
>>>>>>> 7a1ef19 (Projekt)
=======
>>>>>>> 21cd4525f62bc0410ec836db8b005cf7e279bd8c
