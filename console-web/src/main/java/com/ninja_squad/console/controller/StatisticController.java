package com.ninja_squad.console.controller;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.ninja_squad.console.controller.converter.TimeUnitEnumConverter;
import com.ninja_squad.console.model.Statistic;
import com.ninja_squad.console.model.TimeUnit;
import com.ninja_squad.console.repository.StatisticRepository;
import com.ninja_squad.console.utils.TimeUtils;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.resthub.web.controller.RepositoryBasedRestController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

@Controller
@RequestMapping(value = "/api/statistic")
@Slf4j
public class StatisticController extends RepositoryBasedRestController<Statistic, String, StatisticRepository> {

    @Inject
    @Named("statisticRepository")
    @Override
    public void setRepository(StatisticRepository repository) {
        this.repository = repository;
    }

    /**
     * Allow to have cleaner urls as /by/second instead of /by/SECOND
     *
     * @param binder
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        log.debug("binders");
        binder.registerCustomEditor(TimeUnit.class, new TimeUnitEnumConverter());
    }


    @RequestMapping(value = "/per/{unit}", method = RequestMethod.GET)
    @ResponseBody
    public String getStatisticsPerSecond(
            @PathVariable TimeUnit unit, @RequestParam(required = false) Long from,
            @RequestParam(required = false) Long to) {
        log.debug("Stats for " + unit.toString() + (from != null ? " from " + from : "") + (to != null ? " to " + to : ""));
        if (unit == null) { return "[]"; }
        // TODO
        List<Statistic> statsPerSecond = repository.findAllByElementIdAndTimeUnit("", unit);
        return toJson(statsPerSecond, unit, from, to, DateTime.now());
    }

    protected String toJson(List<Statistic> stats, TimeUnit unit, Long from, Long to, DateTime now) {
        if (stats.isEmpty()) { return "[]"; }
        Statistic last = stats.get(0);
        List<Statistic> counts = Lists.newArrayList();
        for (Statistic statistic : stats) {
            //first missing points to zero
            long nextRangeAfterLast = TimeUtils.getNextRange(last.getRange(), unit);
            if (statistic.getRange() > nextRangeAfterLast) {
                Statistic zero = new Statistic(last.getElementId(), nextRangeAfterLast, unit, 0, 0, 0, 0, 0);
                log.debug("add 0 on the first range after last " + zero);
                counts.add(zero);
                last = zero;
            }
            //last missing point to zero
            long previousRangeFromCurrent = TimeUtils.getPreviousRange(statistic.getRange(), unit);
            log.debug("previous from " + statistic.getRange() + " is " + previousRangeFromCurrent);
            if (previousRangeFromCurrent > last.getRange()) {
                Statistic zero = new Statistic(last.getElementId(), previousRangeFromCurrent, unit, 0, 0, 0, 0, 0);
                log.debug("add 0 on the previous range from current " + zero);
                counts.add(zero);
            }
            last = statistic;
            counts.add(statistic);
        }

        //last point to zero (only if the next range fits in the selection)
        long afterLastTimestamp = TimeUtils.getNextRange(last.getRange(), unit);
        long currentTimestampRounded = TimeUtils.getRoundedTimestamp(now.getMillis(), unit);
        if (afterLastTimestamp < currentTimestampRounded) {
            Statistic after = new Statistic(last.getElementId(), afterLastTimestamp, unit, 0, 0, 0, 0, 0);
            log.debug("add 0 on the last point " + after);
            last = after;
            counts.add(after);
        }

        //current point to zero (only if current time is not yet in counts)
        if (currentTimestampRounded > last.getRange()) {
            Statistic current = new Statistic(last.getElementId(), currentTimestampRounded, unit, 0, 0, 0, 0, 0);
            log.debug("add 0 on the current point " + current);
            counts.add(current);
        }

        counts = filterRanges(counts, from, to);

        List<String> json = Lists.transform(counts, new Function<Statistic, String>() {
            @Override
            public String apply(Statistic input) {
                if (input == null) { return "null"; }
                return input.toJson();
            }
        });
        log.debug(json.toString());
        return json.toString();
    }

    protected List<Statistic> filterRanges(List<Statistic> counts, final Long from, final Long to) {
        Predicate<Statistic> isAfterFrom = (from == null) ? Predicates.<Statistic>alwaysTrue() :
                new Predicate<Statistic>() {
                    @Override
                    public boolean apply(Statistic input) {
                        return input.getRange() >= from;
                    }
                };
        Predicate<Statistic> isBeforeTo = (to == null) ? Predicates.<Statistic>alwaysTrue() :
                new Predicate<Statistic>() {
                    @Override
                    public boolean apply(Statistic input) {
                        return input.getRange() <= to;
                    }
                };
        return Lists.newArrayList(Iterables.filter(counts, Predicates.and(isAfterFrom, isBeforeTo)));
    }

}
