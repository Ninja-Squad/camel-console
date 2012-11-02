package com.ninja_squad.console.controller;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.ninja_squad.console.controller.converter.TimeUnitEnumConverter;
import com.ninja_squad.console.model.Statistic;
import com.ninja_squad.console.model.TimeUnit;
import com.ninja_squad.console.repository.StatisticRepository;
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
     * @param binder
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        log.debug("binders");
        binder.registerCustomEditor(TimeUnit.class, new TimeUnitEnumConverter());
    }


    @RequestMapping(value = "/per/{unit}", method = RequestMethod.GET)
    @ResponseBody
    public String getStatisticsPerSecond(@PathVariable TimeUnit unit) {
        log.debug("Stats for " + unit.toString());
        if (unit == null) { return "[]"; }
        List<Statistic> statsPerSecond = repository.findAllByTimeUnit(unit);
        return toJson(statsPerSecond, unit, DateTime.now());
    }

    protected String toJson(List<Statistic> stats, TimeUnit unit, DateTime now) {
        if (stats.isEmpty()) { return "[]"; }
        Statistic last = stats.get(0);
        List<Statistic> counts = Lists.newArrayList();
        for (Statistic statistic : stats) {
            //first missing points to zero
            if (statistic.getRange() - last.getRange() > 1000) {
                Statistic zero = new Statistic(last.getRange() + 1000, unit, 0, 0, 0, 0, 0);
                counts.add(zero);
            }
            //last missing point to zero
            if (statistic.getRange() - last.getRange() > 2000) {
                Statistic zero = new Statistic(statistic.getRange() - 1000, unit, 0, 0, 0, 0, 0);
                counts.add(zero);
            }
            last = statistic;
            counts.add(statistic);
        }
        //last point to zero
        Statistic after = new Statistic(last.getRange() + 1000, unit, 0, 0, 0, 0, 0);
        counts.add(after);
        //current point to zero
        Statistic current = new Statistic(now.getMillis(), unit, 0, 0, 0, 0, 0);
        counts.add(current);

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

}
