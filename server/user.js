function User(name,roomID) {
  this.name = name;
  this.roomID = roomID; 
};



User.prototype.enterRoom = function(roomID) {
  this.roomID=roomID;
};

User.prototype.leaveRoom = function() {
  this.roomID = null; 
};

module.exports = User;