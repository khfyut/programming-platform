"""重设 admin 密码"""
import pymysql

conn = pymysql.connect(
    host='192.168.59.133',
    user='root',
    password='123',
    database='programming_system'
)
cursor = conn.cursor()
cursor.execute(
    "UPDATE user SET password='$2b$12$cb0hLOg5ZHZCpnZXw05Rxu5i0mWfYbjlOmm.ymnRdwULeiNMGg3HW' WHERE username='admin'"
)
conn.commit()
print(f'更新了 {cursor.rowcount} 行，密码重置为 admin123')
conn.close()
