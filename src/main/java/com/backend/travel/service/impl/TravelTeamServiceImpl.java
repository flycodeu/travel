package com.backend.travel.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.backend.travel.POJO.DTO.TravelTeamDto.TravelTeamAddDto;
import com.backend.travel.POJO.DTO.TravelTeamDto.TravelTeamPageDto;
import com.backend.travel.POJO.VO.travelTeam.TravelTeamPageVo;
import com.backend.travel.POJO.entity.Account;
import com.backend.travel.POJO.entity.Travel;
import com.backend.travel.common.CommonConstant;
import com.backend.travel.common.ErrorCode;
import com.backend.travel.execption.BusinessException;
import com.backend.travel.utils.SqlUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.backend.travel.POJO.entity.TravelTeam;
import com.backend.travel.service.TravelTeamService;
import com.backend.travel.dao.TravelTeamMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author admin
 * @description 针对表【travel_team(队伍表)】的数据库操作Service实现
 * @createDate 2023-12-06 15:54:40
 */
@Service
public class TravelTeamServiceImpl extends ServiceImpl<TravelTeamMapper, TravelTeam>
        implements TravelTeamService {
    @Resource
    private AccountServiceImpl accountService;
    @Resource
    private TravelServiceImpl travelService;

    @Override
    public Page<TravelTeamPageVo> getTravelTeamPage(TravelTeamPageDto travelTeamPageDto) {
        Long queryTravelTeamId = travelTeamPageDto.getQueryTravelTeamId();
        Long queryTravelId = travelTeamPageDto.getQueryTravelId();
        String queryTravelTitle = travelTeamPageDto.getQueryTravelTitle();
        Long queryCreateTeamAccountId = travelTeamPageDto.getQueryCreateTeamAccountId();
        String queryUserAccount = travelTeamPageDto.getQueryUserAccount();
        long current = travelTeamPageDto.getCurrent();
        long pageSize = travelTeamPageDto.getPageSize();
        String sortField = travelTeamPageDto.getSortField();
        String sortOrder = travelTeamPageDto.getSortOrder();

        QueryWrapper<TravelTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(queryTravelId != null, "travelId", queryTravelId);
        queryWrapper.eq(queryCreateTeamAccountId != null, "accountId", queryCreateTeamAccountId);
        queryWrapper.eq(queryTravelTeamId != null, "travelTeamId", queryTravelTeamId);

        queryWrapper
                .orderBy(SqlUtils.validSortField(sortField),
                        sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                        sortField);

        Page<TravelTeam> page = this.page(new Page<>(current, pageSize), queryWrapper);

        List<TravelTeamPageVo> teamPageVoList = page.getRecords().stream().map(travelTeam -> {
            TravelTeamPageVo travelTeamPageVo = new TravelTeamPageVo();
            BeanUtil.copyProperties(travelTeam, travelTeamPageVo);
            Long travelId = travelTeam.getTravelId();
            Travel travel = travelService.getOne(new QueryWrapper<Travel>().eq("travelId", travelId));
            Long accountId = travelTeam.getCreateTeamAccountId();
            Account account = accountService.getOne(new QueryWrapper<Account>().eq("accountId", accountId));
            travelTeamPageVo.setTravelTitle(travel.getTravelTitle());
            travelTeamPageVo.setUserAccount(account.getUserAccount());
            return travelTeamPageVo;
        }).collect(Collectors.toList());

        Page<TravelTeamPageVo> teamPageVoPage = new Page<TravelTeamPageVo>(page.getCurrent(), page.getSize());
        teamPageVoPage.setRecords(teamPageVoList);
        teamPageVoPage.setTotal(page.getTotal());
        teamPageVoPage.setPages(page.getPages());
        return teamPageVoPage;
    }

    @Override
    public Boolean addTravelTeam(TravelTeamAddDto travelTeamAddDto) {
        TravelTeam travelTeam = new TravelTeam();
        BeanUtil.copyProperties(travelTeamAddDto, travelTeam);
        boolean save = this.save(travelTeam);
        if (!save) {
            throw new BusinessException(ErrorCode.DATA_INSERT_ERROR);
        }
        return true;
    }
}




