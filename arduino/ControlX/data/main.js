var isRunning   = false;
var temperature = 21;
var request     = new XMLHttpRequest();

function sendNEC(code)
{
	request.open('GET', "/nec?code=" + code);
	request.send();
}

function sendLG(code)
{
	request.open('GET', "/lg?code=" + code);
	request.send();
}

function changeTemp(step)
{
	if (isRunning)
	{
		temperature += step;
		if (temperature > 24)
			temperature = 24;
		if (temperature < 18)
			temperature = 18;
		document.getElementById("state").innerHTML = temperature.toString(10) + "&deg;C";
		runCond();
	}
}

function runCond()
{
	var code = 0;

	var AC_MSBITS1 = 8;
	var AC_MSBITS2 = 8;
	var AC_MSBITS3 = 0;
	var AC_MSBITS4 = 0;
	var AC_MSBITS5 = temperature - 15;
	var AC_MSBITS6 = 4;

	var AC_MSBITS7 = (AC_MSBITS3 + AC_MSBITS4 + AC_MSBITS5 + AC_MSBITS6) & 15;

	code = AC_MSBITS1 << 4;
	code = (code + AC_MSBITS2) << 4;
	code = (code + AC_MSBITS3) << 4;
	code = (code + AC_MSBITS4) << 4;
	code = (code + AC_MSBITS5) << 4;
	code = (code + AC_MSBITS6) << 4;
	code = (code + AC_MSBITS7);

	sendLG("0x" + code.toString(16));
}