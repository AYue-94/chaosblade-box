package com.alibaba.chaosblade.box.service.command.task;

import com.alibaba.chaosblade.box.common.commands.CommandExecutorConstant;
import com.alibaba.chaosblade.box.common.commands.SpringBeanCommand;
import com.alibaba.chaosblade.box.common.common.domain.PageQueryResponse;
import com.alibaba.chaosblade.box.common.common.domain.task.BaseExperimentTask;
import com.alibaba.chaosblade.box.common.experiment.request.ExperimentPageableQueryRequest;
import com.alibaba.chaosblade.box.dao.infrastructure.experiment.task.ExperimentTaskUtil;
import com.alibaba.chaosblade.box.dao.model.ExperimentDO;
import com.alibaba.chaosblade.box.dao.model.ExperimentTaskDO;
import com.alibaba.chaosblade.box.dao.repository.ExperimentRepository;
import com.alibaba.chaosblade.box.dao.repository.ExperimentTaskRepository;
import com.alibaba.chaosblade.box.service.UserService;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author haibin
 *
 *
 */
@Component
@Slf4j
public class ExperimentTaskPageableQueryCommand
    extends SpringBeanCommand<ExperimentPageableQueryRequest, PageQueryResponse<BaseExperimentTask>> {

    @Autowired
    private ExperimentTaskRepository experimentTaskRepository;

    @Autowired
    private ExperimentRepository experimentRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private ExperimentTaskUtil experimentTaskUtil;

    @Override
    public String getCommandExecutorName() {
        return CommandExecutorConstant.EXECUTOR_DEFAULT;
    }

    @Override
    public PageQueryResponse<BaseExperimentTask> execute(
        ExperimentPageableQueryRequest experimentPageableQueryRequest) {
        String experimentId = experimentPageableQueryRequest.getExperimentId();
        int page = experimentPageableQueryRequest.getPage();
        int size = experimentPageableQueryRequest.getSize();
        PageQueryResponse<BaseExperimentTask> pageQueryResponse = new PageQueryResponse<>();
        ExperimentDO experimentDO = experimentRepository.findById(experimentId).orElse(null);
        log.info("ExperimentTaskPageableQueryCommand param:" + JSON.toJSONString(experimentPageableQueryRequest));
        if (experimentDO == null) {
            return pageQueryResponse;
        }
        IPage<ExperimentTaskDO> experimentTaskDOIPage = experimentTaskRepository
            .findByExperimentTasksPageableOrderByCreateTimeDesc(experimentDO.getExperimentId(), page, size);
        pageQueryResponse.setTotal(experimentTaskDOIPage.getTotal());
        pageQueryResponse.setPages(experimentTaskDOIPage.getPages());
        pageQueryResponse.setCurrentPage(experimentTaskDOIPage.getCurrent());
        pageQueryResponse.setPageSize(experimentTaskDOIPage.getSize());
        pageQueryResponse.setContent(Optional.ofNullable(experimentTaskDOIPage.getRecords()).orElse(new ArrayList<>())
            .stream().map(
                new Function<ExperimentTaskDO, BaseExperimentTask>() {
                    @Override
                    public BaseExperimentTask apply(ExperimentTaskDO experimentTaskDO) {
                        BaseExperimentTask experimentTaskBaseInfo = new BaseExperimentTask();
                        experimentTaskUtil.fillBaseInfo(experimentTaskBaseInfo, experimentTaskDO, experimentDO);
                        return experimentTaskBaseInfo;
                    }
                }).collect(Collectors.toList()));
        return pageQueryResponse;
    }

}
