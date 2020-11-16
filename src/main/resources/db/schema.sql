drop table t_equity_position if exists;
drop table t_trade if exists;
drop table t_transaction if exists;
create table t_equity_position (security_code varchar(255) not null, quantity integer, primary key (security_code));
create table t_trade (id bigint unsigned NOT NULL, quantity integer, security_code varchar(255), type integer, primary key (id));
create table t_transaction (id bigint NOT NULL, quantity integer, security_code varchar(255), trade_id bigint, trade_type integer, transaction_type varchar(255), version integer, primary key (id));