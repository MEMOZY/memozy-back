create table device_tokens (
                               id bigint auto_increment primary key,
                               user_id bigint not null,
                               expo_token varchar(255) not null unique,
                               platform varchar(16) not null,
                               is_valid boolean not null default true,
                               created_at timestamp default current_timestamp,
                               constraint fk_device_tokens_user foreign key (user_id) references users(id)
);

CREATE INDEX idx_device_tokens_user_id ON device_tokens(user_id);

CREATE INDEX idx_device_tokens_valid ON device_tokens(is_valid);