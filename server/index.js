// Setup basic express server
var express = require('express');
var app = express();
var server = require('http').createServer(app);
var io = require('socket.io')(server);
var port = process.env.PORT || 3000;
var Room = require('./room.js');
var Trie = require('./trie.js');

var mongoose = require('mongoose');

mongoose.connect('mongodb://127.0.0.1:27017/project');

var roomSchema = new mongoose.Schema({ id: 'string', name: 'string',num_users: 'number' });

var db = mongoose.connection;

var rooms_data = mongoose.model('rooms', roomSchema);


server.listen(port, function () {
  console.log('Server listening at port %d', port);
});

// Routing
app.use(express.static(__dirname + '/public'));

// Chatroom

// usernames which are currently connected to the chat
// room info
//var users = {};
var rooms = {};



rooms_data.find().stream()
  .on('data', function(doc){
    // handle doc
    var room = new Room(doc.name, doc.id ,doc.num_users);
        rooms[doc.id] = room;
        console.log(doc.id);
  })
  .on('error', function(err){
    // handle error
  })
  .on('end', function(){
    // final callback
  });

//var room = new Room("name", socket.roomid , socket.username);


var AutoComplete= new Trie();

io.on('connection', function (socket) {
  var addedUser = false;

  //console.log("asdasdasd");

  socket.on('request topk',function(data){
    socket.emit('topk result', {
        keywords: AutoComplete.getTopk(data.toLowerCase())
      });  

  });

  socket.on('room search', function (data) {
    // we tell the client to execute 'new message'
    
    socket.emit('login', {
      numUsers: 1
    });

    AutoComplete.insertString(data.toLowerCase(),data);

    rooms_data.find({ name: new RegExp(data,'i') },'id',function (err, room_info) {
            // You get a model instance all setup and ready!
      var result = room_info.map(function(r) { return r.id; });



      console.log(result);
//      db.close();       
      socket.emit('search result', {
        roomname: result
      });  
    });
    

    

    // console.log(socket.id);
    console.log(data);
  });




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


  // when the client emits 'add user', this listens and executes
  socket.on('add user', function (data) {
    // we store the username in the socket session for this client
    // console.log(data);

    var parsedData = JSON.parse(data);
    // console.log(parsedData["username"], parsedData["roomid"]);

    socket.username = parsedData["username"];
    socket.roomid = parsedData["roomid"];

    // add the client's username to the global list
    addedUser = true;
    socket.join(socket.roomid);


    var numUsers=1;    
    if(rooms[socket.roomid] == null){
        //create a new room
        var room = new Room("name", socket.roomid,0);
        room.addPerson(socket.username);  
        rooms[socket.roomid] = room;
        var demoRoom = new rooms_data({ id:socket.roomid , name: "name", num_users: 1 });

        demoRoom.save(function (err){
            console.log('Inserted',socket.roomid);
        });
    }else{
        //load data from existing room
        var room = rooms[socket.roomid];
        room.addPerson(socket.username);
        numUsers = room.getUserNumber();
        rooms_data.findOne({ id:socket.roomid }, function (err, doc){
          doc.num_users++;
          doc.save();
        });
    }
    

    // console.log(io.sockets.adapter.rooms[socket.roomid]);
    socket.emit('login', {
      numUsers: numUsers
    });

    // echo globally (all clients) that a person has connected
    socket.broadcast.to(socket.roomid).emit('user joined', {
      username: socket.username,
      numUsers: numUsers
    });
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

  // when the user disconnects.. perform this
  socket.on('disconnect', function () {
    // remove the username from global usernames list
    console.log("disconnect");
      
    if (addedUser) {
      var room = rooms[socket.roomid];

      var numUsers=0;    
      if(room){
        //console.log(room);
        room.removePerson(socket.username);
        numUsers = room.getUserNumber();


        rooms_data.findOne({ id:socket.roomid }, function (err, doc){
          doc.num_users--;
          doc.save();
        });
      }
      
      // echo globally that this client has left
      socket.broadcast.to(socket.roomid).emit('user left', {
        username: socket.username,
        numUsers: numUsers
      });
    }
  });
});
