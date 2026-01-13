package org.ares.cloud.merchantInfo.service.impl;


import cn.hutool.crypto.digest.MD5;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.annotation.Resource;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.ares.cloud.api.user.UserServerClient;
import org.ares.cloud.api.user.dto.UserDto;
import org.ares.cloud.common.dto.PageResult;
import org.ares.cloud.common.enums.ResponseCodeEnum;
import org.ares.cloud.common.exception.BaseException;
import org.ares.cloud.common.exception.RequestBadException;
import org.ares.cloud.database.service.impl.BaseServiceImpl;
import org.ares.cloud.merchantInfo.convert.MerchantInfoConvert;
import org.ares.cloud.merchantInfo.dto.MerchantInfoDto;
import org.ares.cloud.merchantInfo.entity.MerchantFileUploadEntity;
import org.ares.cloud.merchantInfo.entity.MerchantInfoEntity;
import org.ares.cloud.merchantInfo.enums.MerchantError;
import org.ares.cloud.merchantInfo.query.MerchantInfoQuery;
import org.ares.cloud.merchantInfo.repository.MerchantFileUploadRepository;
import org.ares.cloud.merchantInfo.repository.MerchantInfoRepository;
import org.ares.cloud.merchantInfo.service.MerchantInfoService;
import org.ares.cloud.merchantInfo.vo.MerchantInfoVo;
import org.ares.cloud.platformInfo.entity.PlatformApprovalEntity;
import org.ares.cloud.platformInfo.entity.PlatformApprovalRecordEntity;
import org.ares.cloud.platformInfo.enums.ApprovalRecordType;
import org.ares.cloud.platformInfo.enums.SubscribeStatus;
import org.ares.cloud.platformInfo.repository.PlatformApprovalRecordRepository;
import org.ares.cloud.platformInfo.repository.PlatformApprovalRepository;
import org.ares.cloud.redis.cache.RedisCache;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;

/**
* @author hugo tangxkwork@163.com
* @description 商户信息 服务实现
* @version 1.0.0
* @date 2024-10-08
*/
@Service
@AllArgsConstructor
@Slf4j
public class MerchantInfoServiceImpl extends BaseServiceImpl<MerchantInfoRepository, MerchantInfoEntity> implements MerchantInfoService{

    @Resource
    private UserServerClient userServerClient;

    @Resource
    private MerchantInfoConvert convert;


    @Autowired
    private PlatformApprovalRepository platformApprovalRepository;

    @Autowired
    private PlatformApprovalRecordRepository platformApprovalRecordRepository;

    @Autowired
    private MerchantFileUploadRepository merchantFileUploadRepository;

    @Resource
    private RedisCache redisCache;

    /**
    * 创建
    * @param dto 数据模型
    */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public MerchantInfoDto create(MerchantInfoDto dto) {
//        if (StringUtils.isBlank(dto.getCountryCode())){
//            throw new RequestBadException(MerchantError.MERCHANT_INFO_COUNTRY_CODE_NOT_NULL_ERROR);
//        }
        if (StringUtils.isBlank(dto.getRegisterPhone())){
            throw new RequestBadException(MerchantError.MERCHANT_INFO_REGISTER_PHONE_NOT_NULL_ERROR);
        }
//        String countryCode = dto.getCountryCode();
        String registerPhone = dto.getRegisterPhone();
        String id = new MD5().digestHex( registerPhone);
        //判断域名查询商户是否已存在
        QueryWrapper<MerchantInfoEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("domain_name", dto.getDomainName());
        MerchantInfoEntity merchantInfoEntity = baseMapper.selectOne(queryWrapper);
        if (merchantInfoEntity != null) {
            if (!merchantInfoEntity.getId().equals(id)) {
                throw new RequestBadException(MerchantError.MERCHANT_INFO_IS_EXIST_ERROR);
            }
        }
        dto.setStatus(2);
        MerchantInfoEntity entity = convert.toEntity(dto);
        entity.setId(id);
        entity.setIsUpdated("Y");
        int rows = baseMapper.updateById(entity);
        if (rows > 0) {
            redisCache.delete(entity.getDomainName());
        }
        return convert.toDto(entity);
    }

    /**
    * 批量创建
    * @param dos 数据模型集合
    */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(List<MerchantInfoDto> dos) {
        List<MerchantInfoEntity> entities = convert.listToEntities(dos);
        this.saveBatch(entities); 
    }
        
    /**
    * 更新
    * @param dto 数据模型
    */
    @Override
    public void update(MerchantInfoDto dto) {
        MerchantInfoEntity entity = convert.toEntity(dto);
        this.updateById(entity);
    }

    /**
    * 批量更新
    * @param dos 数据模型集合
    */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(List<MerchantInfoDto> dos) {
        List<MerchantInfoEntity> entities = convert.listToEntities(dos);
        this.saveOrUpdateBatch(entities);
    }

    /**
     * 根据id删除
     * @param id  主键
     */
    @Override
    public void deleteById(String id) {
        this.baseMapper.deleteById(id);
    }


    /**
    * 根据ids删除
    * @param ids 主键集合
    */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByIds(List<String> ids) {
        this.baseMapper.deleteBatchIds(ids);
    }

    /**
     * 根据手机号和国家码获取商户信息
     * @param countryCode
     * @param registerPhone
     * @return
     */
    @Override
    public MerchantInfoVo getMerchantInfoByMobile(String countryCode,String registerPhone) {
        MD5 md5 = new MD5();
        String id= md5.digestHex(registerPhone);
        MerchantInfoEntity merchantInfoEntity = this.baseMapper.selectById(id);
        if (merchantInfoEntity!= null){
            String userId = merchantInfoEntity.getUserId();
            UserDto userDto = userServerClient.get(userId);
            if (userDto != null){
                MerchantInfoVo merchantInfoVo = new MerchantInfoVo();
                merchantInfoVo.setUserId(userId);
                //商户id
                String merchantId = userDto.getMerchantId();
                //用户名称
                merchantInfoVo.setUserName(userDto.getNickname());
                //用户邮箱
                merchantInfoVo.setUserEmail(userDto.getEmail());
              /*  //用户手机号
                merchantInfoVo.setPhone(merchantInfoEntity.getPhone());
                //合同编号
                merchantInfoVo.setContractId(merchantInfoEntity.getContractId());
                //企业名称
                merchantInfoVo.setName(merchantInfoEntity.getName());
                //企业域名
                merchantInfoVo.setDomainName(merchantInfoEntity.getDomainName());
                //税务号
                merchantInfoVo.setTaxNum(merchantInfoEntity.getTaxNum());
                //企业电话
                merchantInfoVo.setRegisterPhone(merchantInfoEntity.getRegisterPhone());
                //企业邮箱
                merchantInfoVo.setEnterpriseEmail(merchantInfoEntity.getEnterpriseEmail());
                // iban
                merchantInfoVo.setiBan(merchantInfoEntity.getiBan());
                // bic
                merchantInfoVo.setBic(merchantInfoEntity.getBic());
                //国家
                merchantInfoVo.setCountryCode(merchantInfoEntity.getCountryCode());
                //语言
                merchantInfoVo.setLanguage(merchantInfoEntity.getLanguage());
                //货币
                merchantInfoVo.setCurrency(merchantInfoEntity.getCurrency());
                //礼物点
                merchantInfoVo.setIsOpenGift(merchantInfoEntity.getIsOpenGift());
                //首页展示
                merchantInfoVo.setPageDisplay(merchantInfoEntity.getPageDisplay());
                //企业地址
                merchantInfoVo.setAddress(merchantInfoEntity.getAddress());
                merchantInfoVo.setCreateTime(merchantInfoEntity.getCreateTime());
                merchantInfoVo.setUpdateTime(merchantInfoEntity.getUpdateTime());
                merchantInfoVo.setStatus(merchantInfoEntity.getStatus());
                merchantInfoVo.setIsUpdated(merchantInfoEntity.getIsUpdated());*/
                BeanUtils.copyProperties(merchantInfoEntity,merchantInfoVo);
                //商户总存储 用审批单查询memory-审批记录是use和over的
                Long sumMemory = platformApprovalRecordRepository.selectList(new LambdaQueryWrapper<PlatformApprovalRecordEntity>().
                                eq(PlatformApprovalRecordEntity::getTenantId, merchantId).eq(PlatformApprovalRecordEntity::getRecordType,ApprovalRecordType.SEND.getValue()))
                        .stream().map(item -> item.getChangeMemory()).reduce(0L, Long::sum);

                Long usedMemory = platformApprovalRecordRepository.selectList(new LambdaQueryWrapper<PlatformApprovalRecordEntity>().
                                eq(PlatformApprovalRecordEntity::getTenantId, merchantId).in(PlatformApprovalRecordEntity::getRecordType,
                                        Arrays.asList(ApprovalRecordType.USED.getValue(), ApprovalRecordType.OVER.getValue())))
                        .stream().map(item -> item.getChangeMemory()).reduce(0L, Long::sum);
               //商户总存储减去商户已使用存储然后除以1024再除以1024 转换成BigDecimal类型
                BigDecimal availableMB = BigDecimal.valueOf(sumMemory - usedMemory).divide(BigDecimal.valueOf(1024 ), 2, RoundingMode.DOWN).divide(BigDecimal.valueOf(1024), 2, RoundingMode.DOWN);
                merchantInfoVo.setSumMemory(availableMB);
                //商户已使用存储
                BigDecimal usedMemory1 = BigDecimal.valueOf(usedMemory).divide(BigDecimal.valueOf(1024 *1024), 2, RoundingMode.DOWN);
                merchantInfoVo.setUsedMemory(usedMemory1 );
                //根据商户merchantId查询上次购买审批通过订阅金额
                QueryWrapper<PlatformApprovalEntity> wrapper = new QueryWrapper<>();
                wrapper.eq("user_id", userId);
                wrapper.eq("sub_status", SubscribeStatus.SUB.getValue());
                PlatformApprovalEntity platformApprovalEntity = platformApprovalRepository.selectOne(wrapper);
                //商户上次购买金额
                merchantInfoVo.setLastPurchaseAmount(platformApprovalEntity == null ? BigDecimal.ZERO : platformApprovalEntity.getTotalPrice());
                return  merchantInfoVo;
            }
        }
        //商户信息不存在
        throw new RequestBadException(MerchantError.MERCHANT_INFO_IS_NOT_EXIST_ERROR);
    }

    @Override
    public MerchantInfoVo findByUserId(String userId) {
        if (StringUtils.isNotBlank(userId)){
            QueryWrapper<MerchantInfoEntity> merchantInfoEntityQueryWrapper = new QueryWrapper<>();
            merchantInfoEntityQueryWrapper.eq("user_id",userId);
            MerchantInfoEntity merchantInfoEntity = baseMapper.selectOne(merchantInfoEntityQueryWrapper);
            if (merchantInfoEntity != null){
                MerchantInfoVo merchantInfoVo = new MerchantInfoVo();
                //拷贝属性
                BeanUtils.copyProperties(merchantInfoEntity,merchantInfoVo);
                return merchantInfoVo;
            }
        }
        return null;
    }

    @Override
    public MerchantInfoVo findByDomain(String domain) {
        if (StringUtils.isNotBlank(domain)){
            QueryWrapper<MerchantInfoEntity> merchantInfoEntityQueryWrapper = new QueryWrapper<>();
            merchantInfoEntityQueryWrapper.eq("DOMAIN_NAME",domain);
            MerchantInfoEntity merchantInfoEntity = baseMapper.selectOne(merchantInfoEntityQueryWrapper);
            if (merchantInfoEntity != null){
                MerchantInfoVo merchantInfoVo = new MerchantInfoVo();
                //拷贝属性
                BeanUtils.copyProperties(merchantInfoEntity,merchantInfoVo);
                return merchantInfoVo;
            }
        }
        return null;
    }

    @Override
    public MerchantInfoVo getMerchantInfoByDomainName(String domainName) {
        log.info("getMerchantInfoByDomainName:{}", domainName);
        if (StringUtils.isNotBlank(domainName)) {
            QueryWrapper<MerchantInfoEntity> merchantInfoEntityQueryWrapper = new QueryWrapper<>();
            merchantInfoEntityQueryWrapper.eq("DOMAIN_NAME", domainName);
            MerchantInfoEntity merchantInfoEntity = baseMapper.selectOne(merchantInfoEntityQueryWrapper);
            if (merchantInfoEntity != null) {
                String id = merchantInfoEntity.getId();
                //查询商户MerchantFileUpload数据
                MerchantFileUploadEntity merchantFileUploadEntity = merchantFileUploadRepository.selectById(id);
                MerchantInfoVo merchantInfoVo = new MerchantInfoVo();
                BeanUtils.copyProperties(merchantInfoEntity, merchantInfoVo);
                if (merchantFileUploadEntity != null) {
                    //拷贝属性
                    merchantInfoVo.setLogo(merchantFileUploadEntity.getLogo());
                    return merchantInfoVo;
                }
                return merchantInfoVo;
            } else {
                return null;
            }
        }
        return null;
    }

    @Override
    public List<MerchantInfoVo> getMerchantInfoByName(String name) {
        if (StringUtils.isBlank(name)){
            return List.of(); // 参数为空时返回空列表
        }

        QueryWrapper<MerchantInfoEntity> merchantInfoEntityQueryWrapper = new QueryWrapper<>();
        merchantInfoEntityQueryWrapper.like("name", name);
        List<MerchantInfoEntity> merchantInfoEntities = baseMapper.selectList(merchantInfoEntityQueryWrapper);

        if (merchantInfoEntities == null || merchantInfoEntities.isEmpty()){
            return List.of(); // 查询不到数据时返回空列表
        }

        return merchantInfoEntities.stream()
            .map(item -> {
                MerchantInfoVo merchantInfoVo = new MerchantInfoVo();
                BeanUtils.copyProperties(item, merchantInfoVo);
                return merchantInfoVo;
            })
            .toList();
    }

    /**
    * 根据id获取详情
    * @param ids 主键
    */
    @Override
    public List<MerchantInfoDto> loadByIds(List<String> ids) {
        List<MerchantInfoEntity> entities = this.baseMapper.selectBatchIds(ids);
        return convert.listToDto(entities);
    }

    /**
    * 加载所有数据
    * @return 数据集合
    */
    @Override
    public List<MerchantInfoDto> loadAll() {
        LambdaQueryWrapper<MerchantInfoEntity> wrapper = new LambdaQueryWrapper<>();
        List<MerchantInfoEntity> entities =  this.baseMapper.selectList(wrapper);
        return convert.listToDto(entities);
    }

    /**
    * 查询列表
    * @param query 查询对象
    * @return
    */
    @Override
    public PageResult<MerchantInfoDto> loadList(MerchantInfoQuery query) {
        IPage<MerchantInfoEntity> page = baseMapper.selectPage(getPage(query), getWrapper(query));
        return new PageResult<MerchantInfoDto>(convert.listToDto(page.getRecords()), page.getTotal());
    }

    /**
    * 获取条件
    * @param query
    * @return
    */
    private Wrapper<MerchantInfoEntity> getWrapper(MerchantInfoQuery query){
        LambdaQueryWrapper<MerchantInfoEntity> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isBlank(query.getOrder())){
            wrapper.orderByDesc(MerchantInfoEntity::getId);
        }
        return wrapper;
    }

    private String getAccount(String countryCode ,String phone) {
        if(org.ares.cloud.common.utils.StringUtils.isBlank(countryCode) || org.ares.cloud.common.utils.StringUtils.isBlank(phone)){
            return null;
        }
        if (org.ares.cloud.common.utils.StringUtils.startsWith(countryCode,"+")){
            return countryCode + phone;
        }
        return "+"+countryCode + phone;
    }



    /**
     * 根据id获取商户信息
     * @param id
     * @return
     */
    @Override
    public MerchantInfoDto getMerchantInfoById(String id) {
        log.info("根据id获取商户信息:{}",id);
        MerchantInfoEntity entity = this.baseMapper.selectById(id);
        if (entity != null){
            return  convert.toDto(entity);
        }
        return null;
    }
}