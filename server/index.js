// Setup basic express server
var express = require('express');
var app = express();
var server = require('http').createServer(app);
var io = require('socket.io')(server);
var port = process.env.PORT || 3000;
var Room = require('./room.js');
var Trie = require('./trie.js');
var User = require('./user.js');

var mongoose = require('mongoose');

mongoose.connect('mongodb://127.0.0.1:27017/project');

var roomSchema = new mongoose.Schema({ id: 'string', name: 'string',num_users: 'number', location:{type:[Number], index:'2dsphere'} });
var userSchema = new mongoose.Schema({ name: 'string',roomid: 'string' });


var db = mongoose.connection;

var rooms_data = mongoose.model('rooms', roomSchema);
var users_data = mongoose.model('users', userSchema);


server.listen(port, function () {
  console.log('Server listening at port %d', port);
});

// Routing
app.use(express.static(__dirname + '/public'));

// Chatroom

// usernames which are currently connected to the chat
// room info
var rooms = {};
var users = {};
var AutoComplete= new Trie();


// load data from db
console.log("load room");
rooms_data.find().stream()
  .on('data', function(doc){
    // handle doc
    var room = new Room(doc.name, doc.id ,doc.num_users);
        rooms[doc.id] = room;
        console.log(doc.id);


      users_data.find({ roomid: doc.id },'name',function (err, user_info) {
//             // You get a model instance all setup and ready!
       var result = user_info.map(function(r) { return r.name; });

       console.log(doc.id,result);
      //db.close();
       //socket.emit('search result', {
       //  roomname: result
       //});
      });

  })
  .on('error', function(err){
    // handle error
  })
  .on('end', function(){
    // final callback
  });


console.log("load user");
// load data from db
users_data.find().stream()
  .on('data', function(doc){
    // handle doc
    var user = new User(doc.name, doc.roomid);
     users[doc.name]=user;
    console.log(doc.name);

  })
  .on('error', function(err){
    // handle error
  })
  .on('end', function(){
    // final callback
  });








 


io.on('connection', function (socket) {
  var addedUser = false;
  var loginSuccess= false;

  console.log("connect");

//-------------------------stage 1:login--------------------------------

  socket.on('login',function(data){
    console.log("login");
    if(users[data]){
      socket.emit('login', {
        flag: 3// user conflict
      });
      console.log("login failed");
    }else{

      var new_user = new User(data,null);
      users[data]=new_user;
      socket.emit('login', {
        flag: 0
      });
      loginSuccess=true;

      socket.username = data;

      var demoUser = new users_data({ name: socket.username, rooomid: null });

        demoUser.save(function (err){
            console.log('User inserted',socket.username);
        });

      console.log("logined: "+socket.username);
    }
    
  });



//-------------------------stage 2:search room--------------------------------

  socket.on('request topk',function(data){

  console.log("request topk");
    var result=AutoComplete.getTopk(data.toLowerCase());
    console.log(result);

    socket.emit('topk result', {
        keywords: result
      });
  });

  socket.on('room search', function (data) {
    // we tell the client to execute 'new message'
    //socket.emit('login', {
    //  numUsers: 1
    //});

    console.log("room search");

    AutoComplete.insertString(data.toLowerCase(),data);
    rooms_data.find({ name: new RegExp(data,'i') },'id',function (err, room_info) {
//             // You get a model instance all setup and ready!
       var result = room_info.map(function(r) { return r.id; });

       console.log(result);
      //db.close();
       socket.emit('search result', {
         roomname: result
       });
    });


    //console.log(socket.id);
    //console.log(data);
  });

  


  // when the client emits 'add user', this listens and executes
  // socket.on('add user2', function (data) {
  //   // we store the username in the socket session for this client
  //   // console.log(data);

  //   var parsedData = JSON.parse(data);
  //   // console.log(parsedData["username"], parsedData["roomid"]);

  //   socket.username = parsedData["username"];
  //   socket.roomid = parsedData["roomid"];

  //   // add the client's username to the global list
  //   addedUser = true;
  //   socket.join(socket.roomid);


  //   var numUsers=1;
  //   if(rooms[socket.roomid] == null){
  //       //create a new room
  //       var room = new Room("name", socket.roomid,0);
  //       room.addPerson(socket.username);
  //       rooms[socket.roomid] = room;
  //       var demoRoom = new rooms_data({ id:socket.roomid , name: "name", num_users: 1 });

  //       demoRoom.save(function (err){
  //           console.log('Inserted',socket.roomid);
  //       });
  //   }else{
  //       //load data from existing room
  //       var room = rooms[socket.roomid];
  //       room.addPerson(socket.username);
  //       numUsers = room.getUserNumber();
  //       rooms_data.findOne({ id:socket.roomid }, function (err, doc){
  //         doc.num_users++;
  //         doc.save();
  //       });
  //   }


  //   // console.log(io.sockets.adapter.rooms[socket.roomid]);
  //   socket.emit('login', {
  //     numUsers: numUsers
  //   });

  //   // echo globally (all clients) that a person has connected
  //   socket.broadcast.to(socket.roomid).emit('user joined', {
  //     username: socket.username,
  //     numUsers: numUsers
  //   });
  // });


//-------------------------stage 3:enter room--------------------------------


  socket.on('create room', function (data) {
    // we store the username in the socket session for this client
    // console.log(data);

    console.log('create room');


    var parsedData = JSON.parse(data);
    // console.log(parsedData["username"], parsedData["roomid"]);

    //socket.username = parsedData["username"];
    socket.roomid = parsedData["roomid"];
    socket.roomname = parsedData["roomname"];
 

    var numUsers=1;
    if(rooms[socket.roomid] == null){
        //create a new room
        socket.join(socket.roomid);

        var room = new Room(socket.roomname, socket.roomid,0);
        room.addPerson(socket.username);
        rooms[socket.roomid] = room;
        var demoRoom = new rooms_data({ id:socket.roomid , name: socket.roomname, num_users: 1 });

        demoRoom.save(function (err){
            console.log('Inserted',socket.roomid);
        });

        // console.log(io.sockets.adapter.rooms[socket.roomid]);
        socket.emit('create room', {
          flag:0 
        });
    }else{
        //load data from existing room
        socket.emit('create room', {
          flag: 2//  room conflict
        });
    }
  });



  socket.on('add user', function (data) {
    // we store the username in the socket session for this client
    // console.log(data);

    console.log('add user');

    var parsedData = JSON.parse(data);
    // console.log(parsedData["username"], parsedData["roomid"]);

    //socket.username = parsedData["username"];
    socket.roomid = parsedData["roomid"];


    var numUsers=1;
    if(rooms[socket.roomid] == null){    
        socket.emit('add user', {
          flag: 1// no such room
        });
    }else{
        //load data from existing room
        addedUser = true;
        socket.join(socket.roomid);

        console.log('user joined');

        var room = rooms[socket.roomid];
        room.addPerson(socket.username);
        numUsers = room.getUserNumber();
        rooms_data.findOne({ id:socket.roomid }, function (err, doc){
          doc.num_users++;
          doc.save();
        });



        users_data.findOne({ name:socket.username }, function (err, doc){
          doc.roomid=socket.roomid;
          doc.save();
          
        });

        socket.emit('add user', {
          flag: 0// no such room
        });

        // console.log(io.sockets.adapter.rooms[socket.roomid]);
        socket.emit('user left', {
          numUsers: numUsers
        });

        // echo globally (all clients) that a person has connected
        socket.broadcast.to(socket.roomid).emit('user joined', {
          username: socket.username,
          numUsers: numUsers
        });
    }
    
  });




//-------------------------stage 4:chat--------------------------------

  // when the client emits 'new message', this listens and executes
  socket.on('new message', function (data) {
    // we tell the client to execute 'new message'
    socket.broadcast.to(socket.roomid).emit('new message', {
      username: socket.username,
      message: data
    });

    // console.log(socket.id);
    console.log(socket.username, socket.roomid);
  });

  // when the client emits 'typing', we broadcast it to others
  socket.on('typing', function () {
    socket.broadcast.to(socket.roomid).emit('typing', {
      username: socket.username
    });
  });

  // when the client emits 'stop typing', we broadcast it to others
  socket.on('stop typing', function () {
    socket.broadcast.to(socket.roomid).emit('stop typing', {
      username: socket.username
    });
  });


//-------------------------stage 5:leave room--------------------------------



  socket.on('leave room', function(){

    console.log("left room");
    if (addedUser) {
      var room = rooms[socket.roomid];

      var numUsers=0;
      if(room){
        //console.log(room);
        room.removePerson(socket.username);
        numUsers = room.getUserNumber();


        rooms_data.findOne({ id:socket.roomid }, function (err, doc){
          doc.num_users--;
          if(doc.num_users==0){
            doc.remove();
            //
            delete rooms[socket.roomid];
          }else{
            doc.save();
          }
        });

        users_data.findOne({ name:socket.username }, function (err, doc){
          doc.roomid=null;
          doc.save();
        });
      }

      // echo globally that this client has left
      socket.broadcast.to(socket.roomid).emit('user left', {
        username: socket.username,
        numUsers: numUsers
      });
    }
    addedUser=false;
  });




  // when the user disconnects.. perform this
  socket.on('disconnect', function () {
    // remove the username from global usernames list
//console.log("disconnect");
    if (addedUser) {
      var room = rooms[socket.roomid];

      var numUsers=0;
      if(room){
        //console.log(room);
        room.removePerson(socket.username);
        numUsers = room.getUserNumber();


        rooms_data.findOne({ id:socket.roomid }, function (err, doc){
          doc.num_users--;
          if(doc.num_users==0){
            doc.remove();
            //
            delete rooms[socket.roomid];
          }else{
            doc.save();
          }
        });


        users_data.findOne({ name:socket.username }, function (err, doc){
          doc.roomid=null;
          doc.save();
        });
      }

      // echo globally that this client has left
      socket.broadcast.to(socket.roomid).emit('user left', {
        username: socket.username,
        numUsers: numUsers
      });
    }
    addedUser=false;



    console.log("disconnect");

    if (loginSuccess) {
      delete users[socket.username];


      users_data.findOne({ name:socket.username }, function (err, doc){
          doc.remove();
      });
    }     
    loginSuccess=false;
  });
});

