$jarPath = 'D:\Desktop\毕业设计\Code\System\target\programming-system-1.0.0.jar'
$stdout = 'D:\Desktop\毕业设计\Code\System\loadtest-backend.log'
$stderr = 'D:\Desktop\毕业设计\Code\System\loadtest-backend.err.log'

$command = @(
  'set DB_HOST=192.168.59.133',
  'set DB_PORT=3306',
  'set DB_NAME=programming_system',
  'set DB_USER=root',
  'set DB_PASSWORD=123',
  'set REDIS_HOST=192.168.59.133',
  'set REDIS_PORT=6379',
  'set REDIS_PASSWORD=',
  'set SERVER_PORT=18080',
  'set PROGRAMMING_DOCKER_HOST=tcp://192.168.59.133:2375',
  "java -jar `"$jarPath`" > `"$stdout`" 2> `"$stderr`""
) -join ' && '

cmd /c "start \"loadtest-backend\" /b cmd /c $command"
