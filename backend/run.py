"""Entry point for the backend application."""

from pylogging import HandlerType, setup_logger

from flask_app import server

if __name__ == '__main__':
    server.main()
