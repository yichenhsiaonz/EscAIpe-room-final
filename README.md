# SOFTENG 206 - EscAIpe Room

- This is a point-and-click adventure game created collaboratively with a group of 3.
- Written in Java using the JavaFX library.
- incorporates OpenAI's ChatGPT through a provided API wrapper.
- API wrapper provided was a proxy through UOA servers, so it's no longer functioning.
# Screenshots:
![Screenshot 2024-01-16 151322](https://github.com/yichenhsiaonz/EscAIpe-room-final/assets/79343535/c9785f2c-9e1d-4fe7-aaee-20b81bdf6683)
![Screenshot 2024-01-16 151437](https://github.com/yichenhsiaonz/EscAIpe-room-final/assets/79343535/d782402c-8428-42be-85d7-5bbd99437f3c)
![Screenshot 2024-01-16 151424](https://github.com/yichenhsiaonz/EscAIpe-room-final/assets/79343535/8f92c3bc-cf99-4c67-a4ca-b6158ab7974f)

## To setup OpenAI's API

- requires an OpenAI GPT API key
- add in the root of the project (i.e., the same level where `pom.xml` is located) a file named `apiproxy.config`

  ```
  email: "upi123@aucklanduni.ac.nz"
  apiKey: "YOUR_KEY"
  ```
  these are your credentials to invoke the OpenAI GPT APIs

## To run the game

`./mvnw clean javafx:run`
