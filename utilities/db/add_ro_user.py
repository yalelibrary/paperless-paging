import boto3
import psycopg2
from psycopg2 import sql
import argparse
import json
import secrets
import string


def generate_password(length):
    stringSource = string.ascii_letters + string.digits + '$%^!@'
    password = ''
    for i in range(length):
        password += secrets.choice(stringSource)
    return password


def setup_database(config):
    password_for_ro = generate_password(25)
    username_for_ro = f'{config['ro_user']}'

    username_for_app = config['app']['username']
    password_for_app = config['app']['password']

    print(f'R/O Username: {username_for_ro}')
    print(f'R/O Password: {password_for_ro}')

    info = {
        'ro': {
            'host': config['host'],
            'database': config['database'],
            'username': username_for_ro,
            'password': password_for_ro
        }
    }

    connection = psycopg2.connect(
        user=config['username'],
        password=config['password'],
        host=config['host'],
        port="5432",
        database=config['database'],
    )
    connection.set_session(autocommit=True)
    with connection.cursor() as c:
        #c.execute(sql.SQL('drop owned by {username}').format(username=sql.Identifier(username_for_ro)))
        #c.execute(sql.SQL('drop role if exists {username}').format(username=sql.Identifier(username_for_ro)))
        c.execute(sql.SQL('create user {username} with encrypted password %(password)s').format(username=sql.Identifier(username_for_ro)), {'password': password_for_ro})
        c.execute(sql.SQL('GRANT CONNECT ON DATABASE {database} to {username}').format(database=sql.Identifier(config['database']), username=sql.Identifier(username_for_ro)))
    connection.close()

    connection = psycopg2.connect(
        user=username_for_app,
        password=password_for_app,
        host=config['host'],
        port="5432",
        database=config['database'],
    )
    connection.set_session(autocommit=True)
    with connection.cursor() as c:
        c.execute(sql.SQL('GRANT USAGE ON SCHEMA public to {username}').format(username=sql.Identifier(username_for_ro)))
        c.execute(sql.SQL('GRANT SELECT ON ALL TABLES IN SCHEMA public TO {username}').format(username=sql.Identifier(username_for_ro)))
        c.execute(sql.SQL('ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT ON TABLES TO {username}').format(username=sql.Identifier(username_for_ro)))
    connection.close()
    return info


def store_secrets(name, value):
    client = boto3.client('secretsmanager')
    try:
        client.create_secret(
            ClientRequestToken=secrets.token_urlsafe(),
            Name=name,
            SecretString=json.dumps(value),
        )
    except:
        client.put_secret_value(
            ClientRequestToken=secrets.token_urlsafe(),
            SecretId=name,
            SecretString=json.dumps(value),
        )

def lookup_secrets(id):
    client = boto3.client('secretsmanager')
    secret = client.get_secret_value(SecretId=id)
    return json.loads(secret['SecretString'])

def main():
    parser = argparse.ArgumentParser(description='Load wikidata')
    parser.add_argument('--secret-id', required=True, help='Secret store id')
    parser.add_argument('--secret-id-app', required=True, help='Secret store id for app user')
    parser.add_argument('--database-host', required=True, help='Host for database')
    parser.add_argument('--database-name', required=True, help='Name for database')
    parser.add_argument('--ro-user', required=True, help='RO Usernamae')
    args = parser.parse_args()
    config = lookup_secrets(args.secret_id)
    config['app'] = lookup_secrets(args.secret_id_app)
    config['host'] = args.database_host
    config['database'] = args.database_name
    config['ro_user'] = args.ro_user
    info = setup_database(config)
    secret_id = args.secret_id_app.replace('/app', '')
    print(f'Storing to {secret_id}/{args.ro_user}')
    store_secrets(f'{secret_id}/{args.ro_user}', info['ro'])

if __name__ == '__main__':
    main()