// MongoDB Replica Set Initialization
// This script runs on the first startup to initialize the replica set

// Wait for primary to be ready
sleep(10000);

// Initialize replica set
rs.initiate({
    _id: 'rs0',
    members: [
        {_id: 0, host: 'mongo-primary:27017'},
        {_id: 1, host: 'mongo-secondary:27017'}
    ]
})


print(`Replica set initialized successfully`);
