FROM python:3.5-slim
ADD . /app
WORKDIR /app
RUN pip3 install -r requirements.txt
ENTRYPOINT ["python3", "/app/analyzer.py"]