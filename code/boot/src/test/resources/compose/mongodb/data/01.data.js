print('===================================================');
print('================== MONGO JS INIT ==================');
db.createUser(
  {
    user: 'karate',
    pwd: 'karate-pwd',
    roles: [
      {
        role: 'readWrite',
        db: 'KARATE'
      }
    ]
  }
);
print('===================================================');
print('db.getUsers()= ');
printjson(db.getUsers());
print('===================================================');
print('db.stats()= ');
printjson(db.stats());
print('================== MONGO JS INIT DONE =============');
print('===================================================');
