create table users (
    id bigserial primary key,
    name varchar(255) not null,
    email varchar(255) not null unique,
    password_hash varchar(255) not null,
    role varchar(32) not null,
    created_at timestamptz not null default now()
);

create table categories (
    id bigserial primary key,
    user_id bigint not null references users(id) on delete cascade,
    name varchar(255) not null,
    type varchar(32) not null,
    color varchar(32) not null default '#2563eb',
    created_at timestamptz not null default now(),
    unique (user_id, name, type)
);

create table transactions (
    id bigserial primary key,
    user_id bigint not null references users(id) on delete cascade,
    category_id bigint not null references categories(id),
    type varchar(32) not null,
    amount numeric(12, 2) not null check (amount > 0),
    transaction_date date not null,
    note varchar(500),
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create table budgets (
    id bigserial primary key,
    user_id bigint not null references users(id) on delete cascade,
    category_id bigint not null references categories(id),
    month varchar(7) not null,
    limit_amount numeric(12, 2) not null check (limit_amount > 0),
    created_at timestamptz not null default now(),
    unique (user_id, category_id, month)
);

create index idx_transactions_user_date on transactions(user_id, transaction_date);
create index idx_budgets_user_month on budgets(user_id, month);
