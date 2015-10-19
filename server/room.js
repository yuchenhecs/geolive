function Room(name, id,numUsers) {
  this.name = name;
  this.id = id;
 // this.creator = creator;
  this.people = [];
  this.numUsers = numUsers;
  this.peopleLimit = 4;
  this.status = "available";
  this.private = false;
  
};



Room.prototype.addPerson = function(personID) {
  if (this.status === "available") {
    this.people.push(personID);
  }
  ++this.numUsers;
};

Room.prototype.removePerson = function(person) {
  var personIndex = -1;
  for(var i = 0; i < this.people.length; i++){
    if(this.people[i].id === person.id){
      personIndex = i;
      --this.numUsers;
      break;
    }
  }
  //this.people.remove(personIndex);
  this.people.splice(personIndex,1);

};

Room.prototype.getUserNumber = function() {
  return this.numUsers;
};

Room.prototype.getPerson = function(personID) {
  var person = null;
  for(var i = 0; i < this.people.length; i++) {
    if(this.people[i].id == personID) {
      person = this.people[i];
      break;
    }
  }
  return person;
};

Room.prototype.isAvailable = function() {
  return this.available === "available";
};

Room.prototype.isPrivate = function() {
  return this.private;
};




module.exports = Room;