<p align="center">
  <img src="assets/logo.svg?v=2" width="120" alt="Doorfox logo">
</p>

<h1 align="center">Doorfox</h1>

<p align="center">AI SDR, который квалифицирует лиды из WhatsApp/Instagram и доводит их до визита в офис.</p>

## Что это

Дизайн-студия интерьеров получает 300+ лидов в месяц с рекламы в Instagram — люди пишут в WhatsApp с вопросами о ремонте. Один менеджер физически не может обработать такой поток: часть заявок отвечается с опозданием, часть теряется вовсе. Doorfox — это бот-SDR (Sales Development Representative), который берёт на себя первый контакт: отвечает лиду в WhatsApp (и скоро — в Instagram Direct), выясняет бюджет, тип объекта (квартира/дом/коммерция), сроки и наличие архитектора, а квалифицированных лидов подводит к записи на визит в офис, где сделку уже закрывает человек. В любой момент менеджер может забрать диалог себе через общий инбокс-CRM — бот сразу перестаёт отвечать, пока диалог не вернут обратно.

Проект сделан для одной конкретной студии и её объёма лидов, разворачивается локально, не является open source.

## Функциональность

Что реально работает в коде на данный момент:

- **Приём сообщений через вебхуки** — `WhatsAppWebhookController` и `InstagramWebhookController` принимают входящие сообщения от Meta (с проверкой подписи `X-Hub-Signature-256`), создают лид и диалог при первом обращении и передают текст в `ConversationOrchestrator`.
- **LLM-агностичная квалификация** — `LlmClient` — это интерфейс поверх любого OpenAI-совместимого Chat Completions API. Из коробки настроен на DeepSeek (`deepseek-chat`), но смена провайдера (Qwen/DashScope, OpenRouter и т.д.) — это только смена трёх переменных окружения, без изменения кода.
- **Системный промпт вынесен в файл** — `backend/src/main/resources/prompts/sales-agent-system-prompt.txt` задаёт сценарий квалификации и правила поведения бота (не выдумывать цены, передавать диалог менеджеру при недовольстве клиента и т.п.) отдельно от кода.
- **Общий инбокс с live-обновлениями** — фронтенд на React подписывается на WebSocket (STOMP поверх SockJS, топики `/topic/inbox` и `/topic/conversations/{id}`) и обновляет список диалогов и переписку в реальном времени, без перезагрузки страницы.
- **Перехват диалога менеджером** — `POST /api/conversations/{id}/takeover` и `/release` переключают владельца диалога (`OwnerType.BOT` / `OwnerType.MANAGER`); пока диалог у менеджера, бот не отвечает автоматически.
- **Канбан-доска лидов** — страница Leads отображает лиды по стадиям воронки (`CONTACTED → QUALIFIED → BOOKED → VISITED → SOLD`) с возможностью перетаскивания/смены стадии.
- **JWT-аутентификация** — вход по email/паролю (`AuthController`, bcrypt), дальнейшие запросы — по JWT.

Честно про то, что ещё не готово: отправка исходящих сообщений в WhatsApp (`WhatsAppChannelAdapter`) и Instagram (`InstagramChannelAdapter`), а также бронирование визита в Google Calendar (`NoopCalendarService`) — сейчас это no-op-заглушки. Пока не заданы реальные токены/credentials, они просто логируют, что отправили бы, вместо реального вызова API. Диалог, ответы LLM и вся логика CRM при этом работают полностью — не хватает только выхода вовне.

## Технологии

**Backend**
- Java 21, Spring Boot 3.3.4 (Web, Data JPA, Security, WebSocket, Validation, Actuator)
- PostgreSQL + Flyway (миграции)
- JJWT 0.12.6 для JWT
- Maven

**Frontend**
- React 18.3 + TypeScript, Vite 5.4
- React Query, React Router
- STOMP.js + SockJS для WebSocket
- Tailwind CSS

**Инфраструктура**
- Docker Compose (postgres + backend + frontend), без внешних зависимостей на этапе разработки

## Быстрый старт

Проект рассчитан на локальный запуск (пока без выкладки на сервер).

```bash
git clone https://github.com/TheDarkSolo/doorfox.git
cd doorfox
cp .env.example .env
```

Сгенерируйте настоящий секрет для JWT и вставьте его в `.env` вместо значения по умолчанию:

```bash
openssl rand -base64 48
# вставьте результат в JWT_SECRET= внутри .env
```

Поднимите всё через Docker Compose:

```bash
docker compose up --build -d
```

После запуска:

- Фронтенд (CRM): [http://localhost:8081](http://localhost:8081)
- Backend API: [http://localhost:8090](http://localhost:8090)

Вход в CRM — сидовый аккаунт из миграции `V1__init.sql`:

```
Email:    admin@studio.local
Пароль:   ChangeMe123!
```

**Обязательно смените этот пароль сразу после первого входа** — хеш пароля закоммичен в репозиторий (он публичный для любого, у кого есть доступ к коду), так что дефолтный пароль нельзя считать секретом.

## Настройка

Все переменные окружения описаны в `.env.example`. Пока группа не настроена, соответствующая часть системы не падает — она деградирует до залогированного no-op (кроме БД и JWT, без них ничего работать не будет).

| Группа | Переменные | Статус без настройки |
|---|---|---|
| База данных / JWT | `DB_NAME`, `DB_USER`, `DB_PASSWORD`, `JWT_SECRET`, `CORS_ALLOWED_ORIGINS` | обязательны, без реального `JWT_SECRET` использовать нельзя |
| LLM | `LLM_BASE_URL`, `LLM_API_KEY`, `LLM_MODEL` | бот отвечает заглушкой «LLM не настроен» вместо реальных ответов |
| WhatsApp Cloud API | `WHATSAPP_VERIFY_TOKEN`, `WHATSAPP_APP_SECRET`, `WHATSAPP_ACCESS_TOKEN`, `WHATSAPP_PHONE_NUMBER_ID` | входящие вебхуки принимаются, исходящие сообщения — no-op в логах |
| Instagram Messaging API | `INSTAGRAM_VERIFY_TOKEN`, `INSTAGRAM_APP_SECRET`, `INSTAGRAM_ACCESS_TOKEN`, `INSTAGRAM_IG_USER_ID` | то же самое: приём есть, отправка — no-op |
| Google Calendar | `GOOGLE_CALENDAR_CLIENT_ID`, `GOOGLE_CALENDAR_CLIENT_SECRET` | бронирование визита не создаёт события в календаре (`NoopCalendarService`) |

## Текущий статус / что дальше

Перед тем как запускать бота на реальный поток лидов, нужно:

1. **Получить API-ключ DeepSeek** (или другого OpenAI-совместимого провайдера) и указать его в `LLM_API_KEY` — без него бот не генерирует реальные ответы.
2. **Мигрировать WhatsApp на Cloud API** — сейчас у студии обычный WhatsApp Business (приложение на телефоне); нужен номер, подключённый через Meta Business на Cloud API, чтобы `WHATSAPP_ACCESS_TOKEN`/`WHATSAPP_PHONE_NUMBER_ID` заработали.
3. **Пройти Meta App Review для Instagram Messaging API** (разрешение `instagram_manage_messages`). Это занимает 1–4 недели — стоит подавать заявку заранее, не откладывая на последний момент.
4. **Настроить Google Cloud OAuth** для интеграции с Google Calendar (создание проекта, OAuth consent screen, клиентские credentials), чтобы бронирование визитов реально создавало события.
5. **Заполнить `backend/src/main/resources/prompts/sales-agent-system-prompt.txt` реальными фактами о бизнесе** — услуги и ориентировочные цены, портфолио, адрес и часы работы офиса, типичные возражения клиентов. Сейчас в файле только сценарий и заглушка вместо этих данных; бот намеренно не должен придумывать цифры, которых там нет.
