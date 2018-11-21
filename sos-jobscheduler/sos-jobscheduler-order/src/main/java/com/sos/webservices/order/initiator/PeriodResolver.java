package com.sos.webservices.order.initiator;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.exception.SOSInvalidDataException;
import com.sos.webservices.order.initiator.model.Period;

public class PeriodResolver {

	private static final Logger LOGGER = LoggerFactory.getLogger(PeriodResolver.class);
	private Map<Long, Period> listOfStartTimes;
	private Map<String, Period> listOfPeriods;

	private SimpleDateFormat dateFormat;

	public PeriodResolver() {
		super();
		listOfStartTimes = new HashMap<Long, Period>();
		listOfPeriods = new HashMap<String, Period>();
		dateFormat = new SimpleDateFormat("yyyy-M-dd HH:mm:ss");
	}

	private Date getDate(String day, String time) throws ParseException {
		String dateInString = String.format("%s %s", day, time);
		return dateFormat.parse(dateInString);
	}

	private void logPeriod(Period p) {
		LOGGER.info(p.getBegin() + " - " + p.getEnd());
		LOGGER.info("Single Start: " + p.getSingleStart());
		LOGGER.info("Repeat: " + p.getRepeat());
	}

	private void add(String start, Period period) {
		LOGGER.debug("Adding " + start);
		Period p = listOfPeriods.get(start);
		if (p == null) {
			listOfPeriods.put(start, period);
		} else {
			LOGGER.info("Overlapping period for start time: " + start);
			logPeriod(p);
			logPeriod(period);
		}
	}

	private void addRepeat(Period period) throws ParseException {
        if (!period.getRepeat().isEmpty() && !"00:00:00".equals(period.getRepeat())) {

            Long start = getDate("2000-01-01", period.getBegin()).getTime();
            Long end = getDate("2000-01-01", period.getEnd()).getTime();
            Date repeat = getDate("2000-01-01", period.getRepeat());
            Calendar calendar = GregorianCalendar.getInstance();
            calendar.setTime(repeat);
            long offset = 1000 * (calendar.get(Calendar.HOUR_OF_DAY) * 60 * 60 + calendar.get(Calendar.MINUTE) * 60 + calendar.get(Calendar.SECOND));
            while (offset > 0 && start < end) {
                Date d = new Date(start);
                SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
                String s = formatter.format(d);
                LOGGER.info(s);
                add(s,period);
                start = start + offset;
            }
        }
    }

	private void check(String s, int max) throws SOSInvalidDataException {
		int check = Integer.parseInt(s);
		if (check > max && max > 0) {
			throw new SOSInvalidDataException(s + " increases maximum value: " + max);
		}
	}

	private String normalizeTimeValue(String s) throws SOSInvalidDataException {
		String res = s;
		boolean left = false;
		String format;

		if (res != null && res.startsWith("+")) {
			left = true;
			res = res.substring(1);
		}

		if (res != null && !res.isEmpty()) {

			String[] time = res.split(":");

			if (time.length == 1) {
				check(time[0], 59);
				if (left) {
					format = "00:00:%s";
				} else {
					format = "%s:00:00";
				}
				res = String.format(format, res);
			}
			if (time.length == 2) {

				if (left) {
					format = "00:%s";
				} else {
					format = "%s:00";
				}
				check(time[0], 59);
				check(time[1], 59);
				res = String.format(format, res);
			}
			if (time.length == 3) {
				check(time[0], 24);
				check(time[1], 59);
				check(time[2], 59);
			}
		}
		return res;
	}

	private Period normalizePeriod(Period p) throws SOSInvalidDataException {

		if (p.getBegin() == null || p.getBegin().isEmpty()) {
			p.setBegin("00:00:00");
		}
		if (p.getEnd() == null || p.getEnd().isEmpty()) {
			p.setEnd("24:00:00");
		}

		p.setBegin(normalizeTimeValue(p.getBegin()));
		p.setEnd(normalizeTimeValue(p.getEnd()));
		if (p.getRepeat() == null || p.getRepeat().isEmpty()) {
			p.setRepeat("00:00:00");
		} else {
			p.setRepeat(normalizeTimeValue("+" + p.getRepeat()));
		}
		if (p.getSingleStart() != null && !p.getSingleStart().isEmpty()) {
			p.setSingleStart(normalizeTimeValue(p.getSingleStart()));
		}

		return p;
	}

    public void addStartTimes(Period period) throws ParseException, SOSInvalidDataException {
		period = normalizePeriod(period);
		if (period.getSingleStart() != null && !period.getSingleStart().isEmpty()) {
			add(period.getSingleStart(), period);
		}
		addRepeat(period);
	}

	public Map<Long, Period> getStartTimes() {
		return listOfStartTimes;
	}

	public Map<Long, Period> getStartTimes(String d) throws ParseException {
		listOfStartTimes = new HashMap<Long, Period>();
		for (Entry<String, Period> period : listOfPeriods.entrySet()) {
			listOfStartTimes.put(getDate(d, period.getKey()).getTime(),period.getValue());
		}
		return listOfStartTimes;
	}
}
