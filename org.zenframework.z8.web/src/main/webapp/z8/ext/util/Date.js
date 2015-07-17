Date.monthNames =
[
	"января",
	"февраля",
	"марта",
	"апреля",
	"мая",
	"июня",
	"июля",
	"августа",
	"сентября",
	"октября",
	"ноября",
	"декабря"
];

Date.monthNames1 = 
[
	"Январь",
	"Февраль",
	"Март",
	"Апрель",
	"Май",
	"Июнь",
	"Июль",
	"Август",
	"Сентябрь",
	"Октябрь",
	"Ноябрь",
	"Декабрь"
];

Date.shortMonthNames =
[
	"Янв",
	"Февр",
	"Март",
	"Апр",
	"Май",
	"Июнь",
	"Июль",
	"Авг",
	"Сент",
	"Окт",
	"Нояб",
	"Дек"
];

Date.getShortMonthName = function(month)
{
	return Date.shortMonthNames[month];
};

Date.monthNumbers = 
{
	'Янв': 0,
	'Фев': 1,
	'Мар': 2,
	'Апр': 3,
	'Май': 4,
	'Июн': 5,
	'Июл': 6,
	'Авг': 7,
	'Сен': 8,
	'Окт': 9,
	'Ноя': 10,
	'Дек': 11
};

Date.getMonthNumber = function(name)
{
	return Date.monthNumbers[name.substring(0, 1).toUpperCase() + name.substring(1, 3).toLowerCase()];
};

Date.dayNames =
[
	"Воскресенье",
	"Понедельник",
	"Вторник",
	"Среда",
	"Четверг",
	"Пятница",
	"Суббота"
];

Date.getShortDayName = function(day)
{
	return Date.dayNames[day].substring(0, 3);
};
