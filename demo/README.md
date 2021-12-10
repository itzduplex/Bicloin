# Guião de Demonstração

## 1. Preparação do sistema

Para testar o sistema e todos os seus componentes, é necessário preparar um ambiente com dados para proceder à verificação dos testes.

### 1.1. Lançar o *registry*

Para lançar o *ZooKeeper*, ir à pasta `zookeeper/bin` e correr o comando  
`./zkServer.sh start` (Linux) ou `zkServer.cmd` (Windows).

É possível também lançar a consola de interação com o *ZooKeeper*, novamente na pasta `zookeeper/bin` e correr `./zkCli.sh` (Linux) ou `zkCli.cmd` (Windows).

### 1.2. Compilar o projeto

Primeiramente, é necessário compilar e instalar todos os módulos e suas dependências --  *rec*, *hub*, *app*, etc.
Para isso, basta ir à pasta *root* do projeto e correr o seguinte comando:

```sh
$ mvn clean install -DskipTests
```

### 1.3. Lançar e testar um *rec*

Para proceder aos testes, é preciso em primeiro lugar lançar um servidor *rec* .
Para isso basta ir à pasta *rec* e executar:

```sh
$ mvn compile exec:java
```

ou, caso se queira inicializar mais que uma réplica do servidor *Rec*:

```sh
$ mvn compile exec:java -Dexec.args="localhost 2181 localhost 8091 /grpc/bicloin/rec/1 1"
```

Este comando vai colocar o *rec* no endereço *localhost* e na porta *8091*, com peso *1* (<u>opcional</u>: o último valor equivale ao peso
do *rec* em questão, por omissão o peso é 1), bem como dar bind ao Zookeeper no endreço e porta fornecidos: *localhost* e *2181*.

Para confirmar o funcionamento do servidor com um *ping*, fazer:

```sh
$ cd rec-tester
$ mvn compile exec:java
```

Para executar toda a bateria de testes de integração, fazer:

```sh
$ mvn verify
```

Todos os testes devem ser executados sem erros.


### 1.4. Lançar e testar o *hub*

Para proceder aos testes, é preciso em primeiro lugar lançar o servidor *hub*.
Para isso basta ir à pasta *hub* e executar:

```sh
$ mvn compile exec:java
```

ou, no caso em que se pretende acrescentar um valor de peso total dos servidores *Rec* 
(<u>opcional</u>: último valor do input, número de servidores Rec total que se encontram no ZooKeeper por omissão):

```sh
$ mvn compile exec:java -Dexec.args="localhost 2181 localhost 8081 /grpc/bicloin/hub/1 users.csv stations.csv initRec 5"
```

Este comando vai colocar o *hub* no endreço *localhost* e na porta *8081*.

Para localizar as estações perto da estação *ista*, fazer:

```sh
$ cd hub-tester
$ mvn compile exec:java
```

Para executar toda a bateria de testes de integração, fazer:

```sh
$ mvn verify
```

Todos os testes devem ser executados sem erros.

### 1.5. *App*

Iniciar a aplicação com a utilizadora alice:

```sh
$ app localhost 2181 alice +35191102030 38.7380 -9.3000
```

Para iniciar a aplicação com o utilizador especificado no pom.xml, utilizar:

```sh
$ cd app
$ mvn compile exec:java
```

**Nota:** Para poder correr o script *app* diretamente é necessário fazer `mvn install` e adicionar ao *PATH* ou utilizar diretamente os executáveis gerados na pasta `target/appassembler/bin/`.

Abrir outra consola, e iniciar a aplicação com o utilizador bruno.

Depois de lançar todos os componentes, tal como descrito acima, já temos o que é necessário para usar o sistema através dos comandos.

## 2. Teste dos comandos

Nesta secção vamos correr os comandos necessários para testar todas as operações do sistema.
Cada subsecção é respetiva a cada operação presente no *hub*.

### 2.1. *balance*

O comando balance devolve erro ao utilizador se:
O identificador do utilizador tiver mais que 10 caracteres ou menos que 3, ou não for alfanumérico.

```sh
> balance
bruno 0 BIC
```

Utilizador de teste: *franciscadalmada*
```sh
> balance
INVALID_ARGUMENT: Invalid username
```

### 2.2 *top-up*

O comando top-up devolve erro ao utilizador se:
O identificador do utilizador tiver mais que 10 caracteres ou menos que 3, ou não for alfanumérico.
A quantia a depositar for negativa ou maior que 20€.
O número de telemóvel tiver mais que 15 dígitos ou não for um número válido.

```sh
> top-up 10
bruno 100 BIC
```

User: *danielbatista*
```sh
> top-up 10
INVALID_ARGUMENT: Invalid username
```

Phone number: *t567890987656789876*
```sh
> top-up 10
INVALID_ARGUMENT: Invalid phone number
```

Amount maior que 20:
```sh
> top-up 30
INVALID_ARGUMENT: Invalid amount
```

Telemovel diferente de input file:
```sh
> top-up 10
INVALID_ARGUMENT: Phone number doesnt match 
```

### 2.3 *info*

O comando info devolve erro ao utilizador se:
O identificador da estação não for alfanumérico ou não tiver 4 caracteres.
O identificador da estação não constar nos dados contidos no hub.

```sh
> info ista
IST Alameda, lat 38.7369, -9.1366 long, 20 docas, 3 BIC prémio, 19 bicicletas, 0 levantamentos, 0 devoluções, https://www.google.com/maps/place/38.737613,-9.303164
```

```sh
> info ola
INVALID_ARGUMENT: Invalid station
```

### 2.4 *scan*

O comando scan devolve erro ao utilizador se:
O input é menor ou igual a 0, ou não é um número.

```sh
> scan 3
istt, lat 38.7372, -9.3023 long, 20 docas, 4 BIC prémio, 12 bicicletas, a 87.89062764831701 metros
stao, lat 38.6867, -9.3124 long, 30 docas, 3 BIC prémio, 20 bicicletas, a 5717.703196584258 metros
jero, lat 38.6972, -9.2064 long, 30 docas, 3 BIC prémio, 20 bicicletas, a 9522.166466036724 metros
```

```sh
> scan -1
INVALID_ARGUMENT: Invalid k number given
```

### 2.5 *bike-up*

O comando bike-up devolve erro ao utilizador se:
O identificador do utilizador tiver mais que 10 caracteres ou menos que 3, ou não for alfanumérico.
O identificador da estação não for alfanumérico ou não tiver 4 caracteres.
O identificador da estação não constar nos dados contidos no hub.
O saldo não for suficiente para o aluguer da bicicleta.
O utilizador já tiver uma bicicleta.
Não houver bicicletas na estação pedida.

```sh
> bike-up istt
OK
```

```sh
> bike-up istt
INVALID_ARGUMENT: Insufficient balance
```

```sh
> bike-up amogus
INVALID_ARGUMENT: Invalid station
```

```sh
> bike-up suss
INVALID_ARGUMENT: Station does not exist.
```

```sh
> bike-up stao
INVALID_ARGUMENT: User out of range.
```

```sh
> bike-up istt
OK
> bike-up istt
INVALID_ARGUMENT: User already has bike.
```

### 2.6 *bike-down*

O comando bike-down devolve erro ao utilizador se:
O identificador do utilizador tiver mais que 10 caracteres ou menos que 3, ou não for alfanumérico.
O identificador da estação não for alfanumérico ou não tiver 4 caracteres.
O identificador da estação não constar nos dados contidos no hub.
O utilizador não tiver uma bicicleta.
Não há docas disponíveis na estação pedida.

```sh
> bike-up istt
> bike-down istt
OK
```

```sh
> bike-down istt
INVALID_ARGUMENT: User has no bike.
```

```sh
> bike-down ista
INVALID_ARGUMENT: No docks available.
```

```sh
> bike-up amogus
INVALID_ARGUMENT: Invalid station
```

```sh
> bike-up suss
INVALID_ARGUMENT: Station does not exist.
```

```sh
> bike-up stao
INVALID_ARGUMENT: User out of range.
```


### 2.7 *ping*

O comando ping só devolve erro ao utilizador quando o hub associado não se encontra ativo.

```sh
> ping
Hello Friend!
```

```sh
> ping 
UNAVAILABLE: io exception
```


### 2.8 *sys-status*
O comando sys-status devolve erro se o Hub associado estiver desligado.

```sh
> sys-status
name: /grpc/bicloin/hub/1 isUp: true
name: /grpc/bicloin/rec/1 isUp: true
```

Com o Rec desligado:

```sh
> sys-status
name: /grpc/bicloin/hub/1 isUp: true
name: /grpc/bicloin/rec/1 isUp: false
```

Com o Hub desligado:
```sh
> sys-status
UNAVAILABLE: io exception
```
----

## 3. Considerações Finais

Estes testes não cobrem tudo, pelo que devem ter sempre em conta os testes de integração e o código.
