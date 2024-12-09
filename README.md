# 📌 Task Management API


## 🎯 Функциональность

Этот *RESTful API* позволяет:

- Создавать, редактировать и удалять задачи
- Управлять статусами задач
- Оставлять комментарии к задачам
- Работать с удобным интерфейсом через Swagger UI

---

## 🚀 Как запустить?

### 📋 Предварительные требования

Убедитесь, что у вас установлено следующее:

1. **Java 17** или выше  
   Проверить версию можно командой:
   ```bash
   java -version
2. **Git**
Убедитесь, что *Git* установлен. Скачать *Git* можно [здесь](https://git-scm.com/downloads/).
3. **Docker**  
Убедитесь, что *Docker* установлен и запущен. Скачать *Docker* можно [здесь](https://www.docker.com/).

---

## 🛠 Инструкция по запуску

### Сохраните проект к себе
1. Перейдите в любую папку:
    ``` bash
    cd путь_к_папке
    ```
2. Клонируйте репозиторий:
    ```bash
    git clone https://github.com/MrDenyGrom/task_mang_new.git
    ```
3. Перейдите в директорию проекта:
    ```
    cd task_mang_new
    ```

### Параметры запуска через docker
1. Настройте переменную окружения *JWT_SECRET*:
    Для работы требуется установить секретный токен. Используйте следующий пример и введите его в командную строку:
    Для **Windows**
    ``` bash
    set JWT_SECRET=acb802b56c09b73425d99925464808a9edcbd3026524383c4152b5151eefd8e85abf7033f350fcaf4b189af6a55e4755fbc973adfd72f3abb9a7c8ac75fdf648c88c7b87270993736216dffe954fea841891ecb0a4d721938dea151f9ea71eb6f415f4db757adc62f29881a6ada21be5383ff7e48c04b0751533005f8c7e9bd762e6380f04acc0259afde1cb22df174bc9cf68f7bce56b0edd91247ee62c5a05f835e092a0af37cd0dbd3c414143b87d04ccbf6eb9aaf84f8a7b7b5435cf7c0c8898590301598e846968a1a2506605124836ea33d83f1a10375ada5ca51555ebb3ed65166b1b808f8e665e4a9b6bf60ed725ba8276d369f24090927291907c46
    ```
    Для **Linux/MacOS**
    ``` bash
    export JWT_SECRET=acb802b56c09b73425d99925464808a9edcbd3026524383c4152b5151eefd8e85abf7033f350fcaf4b189af6a55e4755fbc973adfd72f3abb9a7c8ac75fdf648c88c7b87270993736216dffe954fea841891ecb0a4d721938dea151f9ea71eb6f415f4db757adc62f29881a6ada21be5383ff7e48c04b0751533005f8c7e9bd762e6380f04acc0259afde1cb22df174bc9cf68f7bce56b0edd91247ee62c5a05f835e092a0af37cd0dbd3c414143b87d04ccbf6eb9aaf84f8a7b7b5435cf7c0c8898590301598e846968a1a2506605124836ea33d83f1a10375ada5ca51555ebb3ed65166b1b808f8e665e4a9b6bf60ed725ba8276d369f24090927291907c46
    ```
2. Соберите и запустите приложение с помощью **Docker**:
    ``` bash
    docker-compose up --build
    ```
3. Дождитесь, пока все контейнеры будут запущены.

   В случае ошибки:  
    
    ```bash
    [backend 6/9] RUN ./mvnw dependency:go-offline
    
     > [backend 6/9] RUN ./mvnw dependency:go-offline:
    : not foundw: line 20:
    : not foundw: line 31:
    0.266 ./mvnw: set: line 32: illegal option -
    
    failed to solve: process "/bin/sh -c ./mvnw dependency:go-offline" did not complete successfully: exit code: 2
    ```
    
   Скорее всего неправильно установились mvnw и mvnw.cmd. Тогда зайдите в архив и перекиньте в корневую папку проекта те файлы которые там есть и снова попробуйте `docker-compose up --build`.


4. Открытие Swagger UI
    После успешного запуска приложения, вы можете зайти в Swagger UI по следующему адресу:
    http://localhost:8080/swagger-ui.html
    
# 📋Документация API для управления задачами

## 1. Введение

Данный RESTful API предоставляет функциональность для управления задачами. Он позволяет зарегистрированным пользователям создавать, редактировать, удалять задачи, управлять их статусом и оставлять комментарии. 

## 2. Базовый URL

http://localhost:8080

## 3. Аутентификация

API использует JWT авторизацию для контроля доступа к ресурсам. После успешной авторизации на endpoint-e `/login`  вы получите заголовок. 

**Пример**:
**Тело запроса (application/json):**
``` json
{
  "email": "string",
  "password": "string"
}
```

**Ответ:**
```
 authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJzdHJpbmciLCJpYXQiOjE3MzM2ODMwNDMsImV4cCI6MTczMzcxOTA0M30.6nfMOehJTXtl2da375CvCnIGp5yjKWZH5Wp2OyoHEkU-J72K1TnjuC4oISUfFF3ZmTshQ7OSAzD527KyFlpOBQ 
 cache-control: no-cache,no-store,max-age=0,must-revalidate 
 connection: keep-alive 
 content-length: 0 
 date: Sun,08 Dec 2024 18:37:23 GMT 
 expires: 0 
 keep-alive: timeout=60 
 pragma: no-cache 
 vary: Origin,Access-Control-Request-Method,Access-Control-Request-Headers 
 x-content-type-options: nosniff 
 x-frame-options: DENY 
 x-xss-protection: 0 
```

Для автоматической отправки *Bearer Token* на каждый endpoint в Swagger нужно вставить всю остальную строчку после "*authorization: Bearer*" и в правом верхнем углу странице найти **Authorize** и вставить туда *Bearer Token*

## 3.1 Регистрация пользователя

**POST** `/register`

Регистрирует нового пользователя в системе.

**Тело запроса (application/json):**

```json
{
  "password": "string",
  "email": "string"
}
```
**Возвращает сущность *AppUser*:**
``` json
{
  "id": 1,
  "email": "string",
  "enabled": true,
  "locked": false,
  "role": "USER"
}
```

### Ответы:
**201 Created:** *Пользователь успешно зарегистрирован.*

**409 Conflict:** *Пользователь с таким именем уже существует.*

## 3.2 Получение информации о текущем пользователе
**GET** `/me`

Получает информамцию о текущем пользователе.

Возвращается сущность данного пользователя:

``` json
{
  "id": 1,
  "email": "string",
  "enabled": true,
  "locked": false,
  "role": "USER"
}
```
## 3.3 Обновление пароля
**PUT** `/updatePassword`
Обновляет пароль пользователя.

**Тело запроса (application/json):**

```json
{
  "oldPassword": "string",
  "newPassword": "stringst"
}
```

### Ответы:
**200 OK:** *Данные изменены.* 

**400 Bad request:** *Неверный старый пароль.* 

## 4 Создание задачи

**POST** `/api/tasks/create`

Создает новую задачу.

**Тело запроса (application/json):**

``` json
{
  "head": "string",
  "description": "string",
  "status": "WAITING",
  "priority": "CRITICAL",
  "executorUsername": "string",
  "dueDate": "2024-12-08"
}
```

**Возможные статусы задачи:**

*WAITING* - Ожидание,

*IN_PROGRESS* - В работе,

*COMPLETED* - Выполнено,

*CANCELLED* - Отменено,

*ON_HOLD* - Приостановлено,

*IN_REVIEW* - Проверка,

*REJECTED* - Отклонено;

**Возможные приоритеты задачи:**

*CRITICAL* - Критический,

*HIGH* - Высокий,

*MEDIUM* - Средний,

*LOW* - Низкий,

*LOWEST* - Наинизший;

Возвращается объект созданной задачи:

``` json
{
  "id": 1,
  "head": "string",
  "description": "string",
  "status": "WAITING",
  "priority": "CRITICAL",
  "author": {
    "id": 1,
    "email": "string",
    "enabled": true,
    "locked": false,
    "role": "USER"
  },
  "executor": {
    "id": 1,
    "email": "string",
    "enabled": true,
    "locked": false,
    "role": "USER"
  },
  "comments": [],
  "createdAt": "2024-12-08 18:56:33",
  "updatedAt": "2024-12-08 18:56:33",
  "dueDate": "2024-12-08"
}
```

### Ответы:
**201 OK:** *Задача успешно создана.* 

**400 Bad request:** *Неверные данные/не существующий пользователь*.

## 4.2 Редактирование задачи

**PUT** `/api/tasks/edit/{id}`

Редактирует существующую задачу.

**Параметры:**

*id* (long): ID задачи, которую нужно отредактировать.

Тело запроса (application/json):

``` json
{
  "head": "123",
  "description": "string",
  "status": "WAITING",
  "priority": "CRITICAL",
  "executorUsername": "string",
  "dueDate": "2024-12-08"
}
```


Возвращается объект обновленной задачи.

``` json
{
  "id": 1,
  "head": "123",
  "description": "string",
  "status": "WAITING",
  "priority": "CRITICAL",
  "author": {
    "id": 1,
    "email": "string",
    "enabled": true,
    "locked": false,
    "role": "USER"
  },
  "executor": {
    "id": 1,
    "email": "string",
    "enabled": true,
    "locked": false,
    "role": "USER"
  },
  "comments": [],
  "createdAt": "2024-12-08 18:56:33",
  "updatedAt": "2024-12-08 18:59:27",
  "dueDate": "2024-12-08"
}
```

### Ответы:
**200 OK:** *Задача успешно обновлена.*

**403 Forbidden:** *У вас нет прав на редактирование этой задачи (не вы создавали задачу).*

## 4.3 Удаление задачи

**DELETE** `/tasks/delete/{id}`

Удаляет задачу по ее ID.

**Параметры:**

*id* (long): ID задачи, которую нужно удалить.

### Ответы:
**204 OK:** *Задача успешно удалена.*

**403 Forbidden:** *У вас нет прав на удаление этой задачи (не вы создавали задачу).*


## 4.4 Получение задачи по фильтру

**GET** `/api/tasks/get/{id}`

Получает задачу по ее ID.

**Параметры:**

*head* (string): часть заголовка, по которому ищутся задачи, может быть пустой - в таком случае находятся все задачи.

Возвращаются объекты задач:

```json
[
  {
    "id": 2,
    "head": "string",
    "description": "string",
    "status": "WAITING",
    "priority": "CRITICAL",
    "author": {
      "id": 1,
      "email": "string",
      "enabled": true,
      "locked": false,
      "role": "USER"
    },
    "executor": {
      "id": 1,
      "email": "string",
      "enabled": true,
      "locked": false,
      "role": "USER"
    },
    "comments": [],
    "createdAt": "2024-12-08 19:02:52",
    "updatedAt": "2024-12-08 19:02:52",
    "dueDate": "2024-12-08"
  }
]
```

## 4.5 Получение ID всех задач

**GET** `/tasks/get/{id}`

Получает ID всех задач.

Возвращается ID задач:

```json
[
  2,
  3,
  52,
  152
]
```

## 4.6 Получение всех задач

**GET** `/api/tasks/getAll`

Получает все задачи (без пагинации).


Возвращаются объекты задач:

```json
[
  {
    "id": 2,
    "head": "string",
    "description": "string",
    "status": "WAITING",
    "priority": "CRITICAL",
    "author": {
      "id": 1,
      "email": "string",
      "enabled": true,
      "locked": false,
      "role": "USER"
    },
    "executor": {
      "id": 1,
      "email": "string",
      "enabled": true,
      "locked": false,
      "role": "USER"
    },
    "comments": [],
    "createdAt": "2024-12-08 19:02:52",
    "updatedAt": "2024-12-08 19:02:52",
    "dueDate": "2024-12-08"
  }
]
```


## 4.7 Получение задач между датами

**GET** `/api/tasks/getBetween-dates`

Получает задачи между датами.

**Параметры:**

*start* (string YYYY-MM-DD): начальная дата,
*end* (string YYYY-MM-DD): конечная дата.


Возвращаются объекты задачи в уменьшенном виде:

```json
[
  {
    "id": 2,
    "head": "string",
    "description": "string",
    "status": "WAITING",
    "priority": "CRITICAL",
    "authorEmail": "string",
    "executorEmail": "string",
    "createdAt": "2024-12-08 19:02:52",
    "updatedAt": "2024-12-08 19:02:52",
    "dueDate": "2024-12-08"
  }
]
```

## 4.8 Получение всех задач пользователя

**GET** `/api/tasks/getBy-user/{email}`

Получает задачи пользователя.

**Параметры:**

*email* (string): почта пользователя

Возвращаются объекты задачи в уменьшенном виде:

```json
[
  {
    "id": 2,
    "head": "string",
    "description": "string",
    "status": "WAITING",
    "priority": "CRITICAL",
    "authorEmail": "string",
    "executorEmail": "string",
    "createdAt": "2024-12-08 19:02:52",
    "updatedAt": "2024-12-08 19:02:52",
    "dueDate": "2024-12-08"
  }
]
```


## 4.9 Получение задачи по ID

**GET** `/api/tasks/get/{id}`

Получает задачу по ее ID.

**Параметры:**

*id* (integer): ID задачи, которую нужно получить.

Возвращается объект задачи:

```json
{
  "id": 2,
  "head": "string",
  "description": "string",
  "status": "WAITING",
  "priority": "CRITICAL",
  "author": {
    "id": 1,
    "email": "string",
    "enabled": true,
    "locked": false,
    "role": "USER"
  },
  "executor": {
    "id": 1,
    "email": "string",
    "enabled": true,
    "locked": false,
    "role": "USER"
  },
  "comments": [],
  "createdAt": "2024-12-08 19:02:52",
  "updatedAt": "2024-12-08 19:02:52",
  "dueDate": "2024-12-08"
}
```


## 4.10 Получение порученных пользователю задач

**GET** `/api/tasks/my`

Получает задачи, в которых пользователь является исполнителем.

Возвращаются объекты задач:

```json
[
  {
    "id": 2,
    "head": "string",
    "description": "string",
    "status": "WAITING",
    "priority": "CRITICAL",
    "author": {
      "id": 1,
      "email": "string",
      "enabled": true,
      "locked": false,
      "role": "USER"
    },
    "executor": {
      "id": 1,
      "email": "string",
      "enabled": true,
      "locked": false,
      "role": "USER"
    },
    "comments": [],
    "createdAt": "2024-12-08 19:02:52",
    "updatedAt": "2024-12-08 19:02:52",
    "dueDate": "2024-12-08"
  }
]
```


## 4.11 Получение задач с определенным статусом

**GET** `/api/tasks/status/{status}`

**Параметры:**
*status* (Status) : WAITING, IN_PROGRESS, COMPLETED, CANCELLED, ON_HOLD, IN_REVIEW, REJECTED,WAITING

Получает задачи, в которых статус такой же как в запросе.

Возвращаются объекты задач:

```json
[
  {
    "id": 2,
    "head": "string",
    "description": "string",
    "status": "WAITING",
    "priority": "CRITICAL",
    "author": {
      "id": 1,
      "email": "string",
      "enabled": true,
      "locked": false,
      "role": "USER"
    },
    "executor": {
      "id": 1,
      "email": "string",
      "enabled": true,
      "locked": false,
      "role": "USER"
    },
    "comments": [],
    "createdAt": "2024-12-08 19:02:52",
    "updatedAt": "2024-12-08 19:02:52",
    "dueDate": "2024-12-08"
  }
]
```


## 4.12 Назначение задачи пользователю

**GET** `/api/tasks/{taskID}/assign/{userId}`

**Параметры:**
*taskId* (long): ID задачи, в которой нужно поменять исполнителя,
*userId* (long): ID пользователя, который является новым исполнителем

Автор задачи может поменять исполнителя

Возвращается объект задачи:

```json
{
  "id": 2,
  "head": "string",
  "description": "string",
  "status": "WAITING",
  "priority": "CRITICAL",
  "author": {
    "id": 1,
    "email": "string",
    "enabled": true,
    "locked": false,
    "role": "USER"
  },
  "executor": {
    "id": 1,
    "email": "string",
    "enabled": true,
    "locked": false,
    "role": "USER"
  },
  "comments": [],
  "createdAt": "2024-12-08 19:02:52",
  "updatedAt": "2024-12-08 19:02:52",
  "dueDate": "2024-12-08"
}
```
## 4.13 Обновление статуса задачи

**PUT** `/api/tasks/{id}/status`

Обновляет статус задачи - делать это могут только исполнитель/автор.

**Параметры:**

*id* (long): ID задачи, статус которой нужно обновить.

*status* (string): Новый статус задачи (Available values : WAITING, IN_PROGRESS, COMPLETED, CANCELLED, ON_HOLD, IN_REVIEW, REJECTED).

Возвращается объект обновленной задачи.

```json
{
  "id": 2,
  "head": "string",
  "description": "string",
  "status": "COMPLETED",
  "priority": "CRITICAL",
  "author": {
    "id": 1,
    "email": "string",
    "enabled": true,
    "locked": false,
    "role": "USER"
  },
  "executor": {
    "id": 1,
    "email": "string",
    "enabled": true,
    "locked": false,
    "role": "USER"
  },
  "comments": [],
  "createdAt": "2024-12-08 19:02:52",
  "updatedAt": "2024-12-08 19:46:36",
  "dueDate": "2024-12-08"
}
```

### Ответы:
**200 OK:** *Статус задачи успешно обновлен.* 

**403 Forbidden:** *У вас нет прав на изменение статуса этой задачи.*

## 5.1 Создание комментария

**POST** `/api/comments/{taskId}/create`

Создает новый комментарий к задаче.

**Параметры:**

*taskId* (integer): ID задачи, к которой нужно добавить комментарий.

Тело запроса (application/json):
``` json
{
  "text": "string"
}
```

### Ответы:
**201 Created:** *Комментарий успешно создан*.

Возвращается объект созданного комментария:

```json
{
  "id": 1,
  "appUser": {
    "id": 1,
    "email": "string",
    "enabled": true,
    "locked": false,
    "role": "USER"
  },
  "text": "string",
  "createdAt": "2024-12-08 19:51:44",
  "updatedAt": "2024-12-08 19:51:44"
}
```

## 5.2 Количество комментариев пользователя

**GET** `/api/comments/my/count`

Получает количество комментариев пользователя.

Возвращается число комментариев:

```json
1
```

## 5.3 Комментарии пользователя

**GET** `/api/comments/my`

Получает количество комментариев пользователя.

Возвращается объекты комментариев:

```json
[
  {
    "id": 2,
    "head": "string",
    "description": "string",
    "status": "WAITING",
    "priority": "CRITICAL",
    "author": {
      "id": 1,
      "email": "string",
      "enabled": true,
      "locked": false,
      "role": "USER"
    },
    "executor": {
      "id": 1,
      "email": "string",
      "enabled": true,
      "locked": false,
      "role": "USER"
    },
    "comments": [],
    "createdAt": "2024-12-08 19:02:52",
    "updatedAt": "2024-12-08 19:02:52",
    "dueDate": "2024-12-08"
  }
]
```

## 5.4 Поиск комметариев

**GET** `/api/comments/search`

Получает комментарии по ключевому слову.

Возвращается объекты комментариев:

```json
[
  {
    "id": 2,
    "head": "string",
    "description": "string",
    "status": "WAITING",
    "priority": "CRITICAL",
    "author": {
      "id": 1,
      "email": "string",
      "enabled": true,
      "locked": false,
      "role": "USER"
    },
    "executor": {
      "id": 1,
      "email": "string",
      "enabled": true,
      "locked": false,
      "role": "USER"
    },
    "comments": [],
    "createdAt": "2024-12-08 19:02:52",
    "updatedAt": "2024-12-08 19:02:52",
    "dueDate": "2024-12-08"
  }
]
```

## 5.5 Количество комментариев к задаче

**GET** `/api/comments/{taskId}/count`

**Параметры:**

*id* (long): ID задачи, в которой нужно посчитать количество комментариев.

Получает количество комментариев к задаче.

Возвращается число комментариев:

```json
1
```




