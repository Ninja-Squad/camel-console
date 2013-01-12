package com.ninja_squad.console.controller;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
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
import java.util.Map;

@Controller
@RequestMapping(value = "/statistic")
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
     * @param binder to complete
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        log.debug("binders");
        binder.registerCustomEditor(TimeUnit.class, new TimeUnitEnumConverter());
    }


    @RequestMapping(value = "{elementId}/per/{unit}", method = RequestMethod.GET)
    @ResponseBody
    public List<Statistic> getStatisticsPerSecond(@PathVariable String elementId,
                                                  @PathVariable TimeUnit unit, @RequestParam(required = false) Long from,
                                                  @RequestParam(required = false) Long to) {
        Stopwatch stopWatch = new Stopwatch();
        stopWatch.start();
        log.info("Stats for " + elementId + " by " + unit.toString() + (from != null ? " from " + from : "") + (to != null ? " to " + to : ""));
        if (elementId == null || elementId.isEmpty()) {
            return Lists.newArrayList();
        }
        List<Statistic> statsPerSecond = repository.findAllByElementIdAndTimeUnit(elementId, unit);
        log.info("find in " + stopWatch.elapsedMillis());
        List<Statistic> statistics = fillMissingValues(statsPerSecond, unit, from, to, DateTime.now());
        log.info("fill in " + stopWatch.elapsedMillis());
        return statistics;
    }

    /**
     * Adds missing values in stats for all time units between from (or first value if no from) and to (or now if no to)
     *
     * @param stats collection of stats with missing values
     * @param unit  the time unit of the stats
     * @param from  timestamp in millis (lower bound). Can be null
     * @param to    timestamp in millis (higher bound). Can be null
     * @param now   datetime of current request (used if no upper bound is provided)
     * @return a list of stats completed with 0 on missing timestamps
     */
    protected List<Statistic> fillMissingValues(List<Statistic> stats, TimeUnit unit, Long from, Long to, DateTime now) {
        if (stats.isEmpty()) {
            return Lists.newArrayList();
        }

        // map with timestamp -> stat
        Map<Long, Statistic> actuals = Maps.newHashMap();
        for (Statistic stat : stats) {
            actuals.put(stat.getRange(), stat);
        }
        // getting the elementId
        String elementId = stats.get(0).getElementId();
        List<Statistic> counts = Lists.newArrayList();

        // building bounds : lower is from or first stat, upper is to or now
        long timestamp = from != null ? from : stats.get(0).getRange();
        if (to == null) {
            to = TimeUtils.getNextRange(now.getMillis(), unit);
        }
        log.debug("to " + to);
        log.debug("timestamp " + timestamp);

        // building stats with zeros if not find
        while (timestamp <= to) {
            log.debug("timestamp " + timestamp);
            Statistic statistic = actuals.get(timestamp);
            if (statistic == null) {
                statistic = new Statistic(elementId, timestamp, unit, 0, 0, 0, 0, 0);
            }
            counts.add(statistic);
            timestamp = TimeUtils.getNextRange(timestamp, unit);
        }

        log.debug(counts.toString());
        return counts;
    }

    @RequestMapping(value = "{elementId}/aggregated", method = RequestMethod.GET)
    @ResponseBody
    public Statistic aggregateStatistics(@PathVariable String elementId,
                                         @RequestParam(required = true) Long from,
                                         @RequestParam(required = true) Long to) {
        Stopwatch stopWatch = new Stopwatch();
        stopWatch.start();
        Statistic statistic = repository.aggregateStatistics(elementId, from, to);
        log.info("aggregated in " + stopWatch.elapsedMillis());
        return statistic;
    }

}
