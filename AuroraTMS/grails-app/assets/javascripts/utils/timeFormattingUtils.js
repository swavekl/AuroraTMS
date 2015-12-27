//
// Formats event starting day and time into starting day and time e.g. Sun 9:00 am
//
function formatEventDateTime(eventDay, startTime, tournamentStartDate) {
	var mDate = moment([ tournamentStartDate.getFullYear(),
			tournamentStartDate.getMonth(), tournamentStartDate.getDate() ]);
	if (eventDay > 1) {
		mDate = mDate.add(eventDay - 1, 'days');
	}
	var hours = 0;
	var minutes = 0;
	// test if they chose 30 minutes past hour start time
	if (startTime == Math.floor(startTime)) {
		hours = startTime;
		minutes = 0;
	} else {
		// he chose 8:30 or 9:30 etc.
		hours = Math.floor(startTime);
		minutes = 30;
	}
	// console.log ('just date' + mDate.format());
	// console.log ('hours ' + hours + " minutes " + minutes);
	mDate.utcOffset(0);
	mDate.utc();
	mDate.hours(hours);
	mDate.minutes(minutes);
	mDate.seconds(0);
	return mDate.format('ddd h:mm A'); // Sun 1:30 PM
}
