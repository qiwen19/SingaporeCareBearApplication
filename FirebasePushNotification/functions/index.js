'use-strict' 
const functions = require('firebase-functions'); 
const admin = require('firebase-admin'); 
admin.initializeApp(functions.config().firebase); 

exports.sendNotification = functions.firestore.document('users/{user_id}/Notifications/{notification_id}').onWrite((change, context)=> {
	
	const user_id = context.params.user_id; 
	const notification_id = context.params.notification_id; 
	console.log('User ID: ' + user_id + ' | notification ID: ' + notification_id); 
	
	return admin.firestore().collection('users').doc(user_id).collection('Notifications').doc(notification_id).get().then(queryResult =>{
		
		const from_user_id = queryResult.data().from;
		const from_message = queryResult.data().message;
		
		const from_data = admin.firestore().collection('users').doc(from_user_id).get();
		const to_data = admin.firestore().collection('users').doc(user_id).get();
		
		return Promise.all([from_data, to_data]).then(result => {
			
			const from_name = result[0].data().fullname;
			const to_name = result[1].data().fullname;
			const token_id = result[1].data().token_id;
			
			//console.log('FROM: ' + from_name + 'TO: ' + to_name);
			const payload = {
				notification: {
					title: "REQUEST FOR HELP",
					body: from_name + " had requested for help",
					icon: "default"
				}
			};
			
			return admin.messaging().sendToDevice(token_id, payload).then((response) =>{
				console.log('Notification Sent');
				return true;
			});
		
		});
	});
});

