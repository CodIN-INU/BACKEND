// MongoDB initialization script for CODIN MSA
// This script creates separate databases for each microservice

// Create databases for each service
db = db.getSiblingDB('codin-auth');
db.createCollection('users');
db.createCollection('tokens');

db = db.getSiblingDB('codin-user');
db.createCollection('users');
db.createCollection('follows');
db.createCollection('profiles');

db = db.getSiblingDB('codin-content');
db.createCollection('posts');
db.createCollection('comments');
db.createCollection('likes');
db.createCollection('images');

db = db.getSiblingDB('codin-notification');
db.createCollection('notifications');
db.createCollection('notification_settings');
db.createCollection('fcm_tokens');

db = db.getSiblingDB('codin-chat');
db.createCollection('chatrooms');
db.createCollection('messages');
db.createCollection('participants');

// Create indexes for better performance
db = db.getSiblingDB('codin-auth');
db.users.createIndex({ "email": 1 }, { unique: true });
db.users.createIndex({ "studentId": 1 }, { unique: true });
db.tokens.createIndex({ "token": 1 });
db.tokens.createIndex({ "expiredAt": 1 }, { expireAfterSeconds: 0 });

db = db.getSiblingDB('codin-user');
db.users.createIndex({ "email": 1 });
db.users.createIndex({ "studentId": 1 });
db.follows.createIndex({ "followerId": 1, "followingId": 1 }, { unique: true });

db = db.getSiblingDB('codin-content');
db.posts.createIndex({ "authorId": 1 });
db.posts.createIndex({ "createdAt": -1 });
db.comments.createIndex({ "postId": 1 });
db.comments.createIndex({ "authorId": 1 });
db.likes.createIndex({ "postId": 1, "userId": 1 }, { unique: true });

db = db.getSiblingDB('codin-notification');
db.notifications.createIndex({ "userId": 1 });
db.notifications.createIndex({ "createdAt": -1 });
db.fcm_tokens.createIndex({ "userId": 1 });

db = db.getSiblingDB('codin-chat');
db.chatrooms.createIndex({ "participants": 1 });
db.messages.createIndex({ "chatroomId": 1 });
db.messages.createIndex({ "createdAt": -1 });
db.participants.createIndex({ "chatroomId": 1, "userId": 1 }, { unique: true });

print('CODIN MSA databases and collections created successfully!');
