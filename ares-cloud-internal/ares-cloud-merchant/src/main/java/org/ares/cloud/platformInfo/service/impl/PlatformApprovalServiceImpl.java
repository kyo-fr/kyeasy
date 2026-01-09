package org.ares.cloud.platformInfo.service.impl;


import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.crypto.digest.MD5;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.annotation.Resource;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.ares.cloud.api.msg_center.NotificationServerClient;
import org.ares.cloud.api.msg_center.command.SendNotificationCommand;
import org.ares.cloud.api.msg_center.enums.NotificationType;
import org.ares.cloud.api.order.OrderClient;
import org.ares.cloud.api.order.commod.CreateOrderCommand;
import org.ares.cloud.api.order.commod.PayCommand;
import org.ares.cloud.api.order.commod.PayOrderCommand;
import org.ares.cloud.api.order.enums.ApiOrderType;
import org.ares.cloud.api.order.enums.ApiPaymentModel;
import org.ares.cloud.api.user.UserServerClient;
import org.ares.cloud.api.user.dto.UserDto;
import org.ares.cloud.api.user.enums.UserIdentity;
import org.ares.cloud.common.context.ApplicationContext;
import org.ares.cloud.common.dto.PageResult;
import org.ares.cloud.common.enums.ResponseCodeEnum;
import org.ares.cloud.common.exception.BaseException;
import org.ares.cloud.common.exception.BusinessException;
import org.ares.cloud.common.exception.RequestBadException;
import org.ares.cloud.database.service.impl.BaseServiceImpl;
import org.ares.cloud.exception.RpcCallException;
import org.ares.cloud.merchantInfo.entity.MerchantInfoEntity;
import org.ares.cloud.merchantInfo.repository.MerchantInfoRepository;
import org.ares.cloud.platformInfo.convert.PlatformApprovalConvert;
import org.ares.cloud.platformInfo.dto.PlatformApprovalDto;
import org.ares.cloud.platformInfo.dto.PlatformApprovalSettlementDto;
import org.ares.cloud.platformInfo.dto.PlatformSubscribeDto;
import org.ares.cloud.platformInfo.entity.PlatformApprovalEntity;
import org.ares.cloud.platformInfo.entity.PlatformApprovalRecordEntity;
import org.ares.cloud.platformInfo.entity.PlatformInfoEntity;
import org.ares.cloud.platformInfo.entity.PlatformTaxRateEntity;
import org.ares.cloud.platformInfo.enums.ApprovalRecordType;
import org.ares.cloud.platformInfo.enums.ApprovalType;
import org.ares.cloud.platformInfo.enums.PlatformError;
import org.ares.cloud.platformInfo.enums.SubscribeStatus;
import org.ares.cloud.platformInfo.mapper.InvoiceMapper;
import org.ares.cloud.platformInfo.mapper.UserMapper;
import org.ares.cloud.platformInfo.query.PlatformApprovalQuery;
import org.ares.cloud.platformInfo.repository.PlatformApprovalRecordRepository;
import org.ares.cloud.platformInfo.repository.PlatformApprovalRepository;
import org.ares.cloud.platformInfo.repository.PlatformInfoRepository;
import org.ares.cloud.platformInfo.repository.PlatformTaxRateRepository;
import org.ares.cloud.platformInfo.service.PlatformApprovalService;
import org.ares.cloud.platformInfo.service.PlatformSubscribeService;
import org.ares.cloud.platformInfo.vo.PlatformApprovalRecordVo;
import org.ares.cloud.platformInfo.vo.PlatformApprovalVo;
import org.springframework.beans.BeanUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author hugo tangxkwork@163.com
 * @version 1.0.0
 * @description 平台审批 服务实现
 * @date 2024-10-31
 */
@Service
@AllArgsConstructor
@Slf4j
public class PlatformApprovalServiceImpl extends BaseServiceImpl<PlatformApprovalRepository, PlatformApprovalEntity> implements PlatformApprovalService {

    @Resource
    private PlatformApprovalConvert convert;
    @Resource
    private PlatformSubscribeService platformSubscribeService;

    @Resource
    private UserServerClient userServerClient;

    @Resource
    private OrderClient orderClient;

    @Resource
    private PlatformTaxRateRepository platformTaxRateRepository;

    @Resource
    private PlatformApprovalRecordRepository platformApprovalRecordRepository;

    @Resource
    private PlatformInfoRepository platformInfoRepository;

    @Resource
    private MerchantInfoRepository merchantInfoRepository;


    @Resource
    private UserMapper userMapper;

    @Resource
    private PlatformApprovalRepository platformApprovalRepository;

    @Resource
    private InvoiceMapper invoiceMapper;

    @Resource
    private NotificationServerClient notificationServerClient;

    /**
     * 创建
     *
     * @param dto 数据模型
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public PlatformApprovalDto create(PlatformApprovalDto dto) throws BaseException {
        //根据类型查询开户订阅审批单是否已存在
        if (dto.getType().equals(ApprovalType.OPEN_MERCHANT.getValue())){
            PlatformApprovalEntity platformApprovalEntity = platformApprovalRepository.selectOne(new QueryWrapper<PlatformApprovalEntity>().eq("type", ApprovalType.OPEN_MERCHANT.getValue()).eq("user_id", dto.getUserId()));
            if (platformApprovalEntity != null) {
                throw new RequestBadException(PlatformError.PLATFORM_USER_APPROVAL_EXIST_ERROR);
            }
        }else {
            UserDto userDto = userServerClient.get(dto.getUserId());
            if (userDto != null) {
                if (userDto.getIdentity() != UserIdentity.Merchants.getValue()) {
                    throw new RequestBadException(PlatformError.PLATFORM_USER_IS_NOT_MERCHANT_ERROR);
                }
            }
        }
        //税务号不能重复
        PlatformApprovalEntity taxEntity = baseMapper.selectOne(new QueryWrapper<PlatformApprovalEntity>().eq("tax_num", dto.getTaxNum()));
        if (taxEntity != null) {
            throw new RequestBadException(PlatformError.PLATFORM_TAX_NUM_EXIST_ERROR);
        }
        //根据订阅类型查询订阅单价
        PlatformSubscribeDto platformSubscribeDto = platformSubscribeService.loadById(dto.getMerchantSubscribeId());
        if (platformSubscribeDto == null) {
            throw new RequestBadException(PlatformError.PLATFORM_SUBSCRIBE_NOT_EXIST_ERROR);
        }
        //线下支付需要生成审批单、 线上支付走支付接口进行回调 单独开个接口进行用户身份变更为商户
        //上面逻辑弃用， 线上线下都需要生成审批工单， 线上生成工单后，返回前端订单号，前端唤起线上支付进行付款，后续支付成功回调 进行工单状态变更
        //线下支付需要审核
//        if (dto.getChannelType() == 1) {
            //生成审批单id
            PlatformApprovalEntity entity = convert.toEntity(dto);
            PlatformInfoEntity platformInfo = platformInfoRepository.selectOne(null);
            if (Objects.isNull(platformInfo)){
                throw new RequestBadException(PlatformError.PLATFORM_INFO_NOT_EXIST_ERROR);
            }
            //组装订单参数
            CreateOrderCommand createOrderCommand = new CreateOrderCommand();
            createOrderCommand.setOrderType(ApiOrderType.SUBSCRIPTION);
            createOrderCommand.setMerchantId(platformInfo.getId());
            ApplicationContext.setTenantId(platformInfo.getId());
            createOrderCommand.setServiceFee(new BigDecimal(0));
            createOrderCommand.setPaymentMode(ApiPaymentModel.OFFLINE);
            CreateOrderCommand.OrderItemCommand orderItemCommand = new CreateOrderCommand.OrderItemCommand();
            orderItemCommand.setProductId("1");
            orderItemCommand.setProductName("存储订阅");
            orderItemCommand.setQuantity(1);

            BigDecimal price = platformSubscribeDto.getPrice();
            //price*12*dto.getMemory()
            Integer month = platformSubscribeDto.getMonth();
            BigDecimal  totalPrice =  price.multiply(new BigDecimal(month)).multiply(new BigDecimal(dto.getMemory()));
            orderItemCommand.setUnitPrice(totalPrice);
            createOrderCommand.setPaymentChannelId(dto.getPaymentChannelId());
            createOrderCommand.setItems(CollectionUtil.newArrayList(orderItemCommand));
            ApplicationContext.setUserId(dto.getUserId());
            log.info("createOrderCommand:{}", JSONObject.toJSONString(createOrderCommand));
            try {
                //订单保存
                String orderId = orderClient.createOrder(createOrderCommand);
                if (orderId != null && !orderId.isEmpty()) {
                    entity.setStatus(1);
                    entity.setOrderId(orderId);
                    entity.setMonth(month);
                    entity.setTotalPrice(totalPrice);
                    entity.setMemory(dto.getMemory());
                    entity.setStartTime(DateUtil.date().getTime());
                    entity.setEndTime(DateUtil.offsetDay(DateUtil.date(), 365).getTime());
                    entity.setSubscribeType(platformSubscribeDto.getSubscribeType());
                    entity.setSubStatus(SubscribeStatus.SUB.getValue());
                    //审批单保存
                    ApplicationContext.setIgnoreTenant(true);
                    QueryWrapper<PlatformTaxRateEntity> queryWrapper = new QueryWrapper<>();
                    PlatformTaxRateEntity platformTaxRateEntity = platformTaxRateRepository.selectOne(queryWrapper.eq("type", "4"));
                    if (platformTaxRateEntity != null) {
                        //税率
                        entity.setTaxRate(platformTaxRateEntity.getTaxRate());
                    }
                    ApplicationContext.setIgnoreTenant(true);
                    int insert = this.baseMapper.insert(entity);
                    if (insert > 0) {
                            //调用通知接口-平台新增一笔审批单
                            SendNotificationCommand sendNotificationCommand = new SendNotificationCommand();
                            sendNotificationCommand.setType(NotificationType.NEW_APPROVAL);
                            sendNotificationCommand.setReceiver("platform");
                            sendNotificationCommand.setTitle("新增审批单");
                            sendNotificationCommand.setContent("您有一笔新审批单，请及时处理");
                            sendNotificationCommand.setData(entity);
                            sendNotificationCommand.setTimestamp(System.currentTimeMillis());
                            try {
                                notificationServerClient.sendNotification(sendNotificationCommand);
                            } catch (Exception e) {
                                log.error("通知平台新增一笔审批单失败", e);
                            }

                    }
                    String approvalId = entity.getId();
                    entity.setApprovalId(approvalId);
                    return convert.toDto(entity);
                }
            } catch (Exception e) {
                log.error("创建工单失败", e);
                throw new RuntimeException("创建工单失败", e);
            }
//        }
        return null;
    }

    /**
     * 批量创建
     *
     * @param dos 数据模型集合
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(List<PlatformApprovalDto> dos) {
        List<PlatformApprovalEntity> entities = convert.listToEntities(dos);
        this.saveBatch(entities);
    }

    /**
     * 更新
     *
     * @param dto 数据模型
     */
    @Override
    public void update(PlatformApprovalDto dto) {
        PlatformApprovalEntity entity = convert.toEntity(dto);
        this.updateById(entity);
    }

    /**
     * 批量更新
     *
     * @param dos 数据模型集合
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(List<PlatformApprovalDto> dos) {
        List<PlatformApprovalEntity> entities = convert.listToEntities(dos);
        this.saveOrUpdateBatch(entities);
    }

    /**
     * 根据id删除
     *
     * @param id 主键
     */
    @Override
    public void deleteById(String id) {
        this.baseMapper.deleteById(id);
    }

    /**
     * 根据ids删除
     *
     * @param ids 主键集合
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByIds(List<String> ids) {
        this.baseMapper.deleteBatchIds(ids);
    }

    /**
     * 根据id获取详情
     *
     * @param id 主键
     */
    @Override
    public PlatformApprovalVo loadById(String id) {
        PlatformApprovalEntity entity = this.baseMapper.selectById(id);
        if (entity != null) {
            PlatformApprovalVo platformApprovalVo = new PlatformApprovalVo();
            //根据订阅id查询订阅类型
            PlatformSubscribeDto platformSubscribeDto = platformSubscribeService.loadById(entity.getMerchantSubscribeId());
            if (platformSubscribeDto != null) {
                platformApprovalVo.setSubscribeType(platformSubscribeDto.getSubscribeType());
//                platformApprovalVo.setId(entity.getId());
                platformApprovalVo.setApprovalId(entity.getId());
//                platformApprovalVo.setOrderId(entity.getOrderId());
//                platformApprovalVo.setStatus(entity.getStatus());
//                platformApprovalVo.setUserId(entity.getUserId());
                //存储主数据价格
//                platformApprovalVo.setPrice(platformSubscribeDto.getPrice());
                //总价
//                platformApprovalVo.setTotalPrice(entity.getTotalPrice());
//                platformApprovalVo.setMonth(entity.getMonth());
//                platformApprovalVo.setNum(1);
//                platformApprovalVo.setCreateTime(entity.getCreateTime());
//                platformApprovalVo.setName(entity.getName());
                BeanUtils.copyProperties(entity, platformApprovalVo);
                QueryWrapper<PlatformTaxRateEntity> queryWrapper = new QueryWrapper<>();
                //查询税率4-订阅税率(平台)
                PlatformTaxRateEntity platformTaxRateEntity = platformTaxRateRepository.selectOne(queryWrapper.eq("type", "4"));
                if (platformTaxRateEntity != null) {
                    log.info("taxRate:{}", JSONObject.toJSONString(platformTaxRateEntity));
                    //税率
                    platformApprovalVo.setTaxRate(platformTaxRateEntity.getTaxRate());
                }
                return platformApprovalVo;
            }
        }
        return null;
    }

    /**
     * 根据id获取详情
     *
     * @param ids 主键
     */
    @Override
    public List<PlatformApprovalDto> loadByIds(List<String> ids) {
        List<PlatformApprovalEntity> entities = this.baseMapper.selectBatchIds(ids);
        return convert.listToDto(entities);
    }

    /**
     * 加载所有数据
     *
     * @return 数据集合
     */
    @Override
    public List<PlatformApprovalDto> loadAll() {
        LambdaQueryWrapper<PlatformApprovalEntity> wrapper = new LambdaQueryWrapper<>();
        List<PlatformApprovalEntity> entities = this.baseMapper.selectList(wrapper);
        return convert.listToDto(entities);
    }

    /**
     * 查询列表
     *
     * @param query 查询对象
     * @return
     */
    @Override
    public PageResult<PlatformApprovalVo> loadList(PlatformApprovalQuery query) {
        IPage<PlatformApprovalEntity> page = baseMapper.selectPage(getPage(query), getWrapper(query));
        if (CollectionUtil.isEmpty(page.getRecords())) {
            return new PageResult<>(new ArrayList<>(), 0);
        }
        List<PlatformApprovalVo> list = new ArrayList<>();
        for (PlatformApprovalEntity entity : page.getRecords()) {
            PlatformApprovalVo vo = new PlatformApprovalVo();
            //根据用户id查询用户手机号
            UserDto userDto = userServerClient.get(entity.getUserId());
            if (userDto != null) {
                vo.setPhone(userDto.getPhone());
                vo.setAccount(userDto.getAccount());
            }
            //订阅信息
            PlatformSubscribeDto platformSubscribeDto = platformSubscribeService.loadById(entity.getMerchantSubscribeId());
            if (platformSubscribeDto != null) {
                vo.setSubscribeType(platformSubscribeDto.getSubscribeType());
                vo.setPrice(platformSubscribeDto.getPrice());
            }
            //根据订单号查询发票id
            String invoiceId = invoiceMapper.getInvoiceByOrderId(entity.getOrderId());
            if (StringUtils.isNotBlank(invoiceId)){
                vo.setInvoiceId(invoiceId);
            }
            QueryWrapper<PlatformTaxRateEntity> queryWrapper = new QueryWrapper<>();
            PlatformTaxRateEntity platformTaxRateEntity = platformTaxRateRepository.selectOne(queryWrapper.eq("type", "4"));
            if (platformTaxRateEntity != null) {
                //税率
                entity.setTaxRate(platformTaxRateEntity.getTaxRate());
            }
            // 基础信息
            BeanUtils.copyProperties(entity, vo);
            vo.setApprovalId(entity.getId());
            list.add(vo);
        }
        return new PageResult<>(list, page.getTotal());
    }


    /**
     * 查询列表
     *
     * @param query 查询对象
     * @return
     */
    @Override
    public PageResult<PlatformApprovalVo> income(PlatformApprovalQuery query) {
        IPage<PlatformApprovalEntity> page = baseMapper.selectPage(getPage(query), getIncomeWrapper(query));
        if (CollectionUtil.isEmpty(page.getRecords())) {
            return new PageResult<>(new ArrayList<>(), 0);
        }
        List<PlatformApprovalVo> list = new ArrayList<>();
        for (PlatformApprovalEntity entity : page.getRecords()) {
            PlatformApprovalVo vo = new PlatformApprovalVo();
            //根据用户id查询用户手机号
            UserDto userDto = userServerClient.get(entity.getUserId());
            if (userDto != null) {
                vo.setPhone(userDto.getPhone());
                vo.setAccount(userDto.getAccount());
            }
            //订阅信息
            PlatformSubscribeDto platformSubscribeDto = platformSubscribeService.loadById(entity.getMerchantSubscribeId());
            if (platformSubscribeDto != null) {
                vo.setSubscribeType(platformSubscribeDto.getSubscribeType());
                vo.setPrice(platformSubscribeDto.getPrice());
            }
            //根据订单号查询发票id
            String invoiceId = invoiceMapper.getInvoiceByOrderId(entity.getOrderId());
            if (StringUtils.isNotBlank(invoiceId)){
                vo.setInvoiceId(invoiceId);
            }
            QueryWrapper<PlatformTaxRateEntity> queryWrapper = new QueryWrapper<>();
            //查询税率4-订阅税率(平台)
            PlatformTaxRateEntity platformTaxRateEntity = platformTaxRateRepository.selectOne(queryWrapper.eq("type", "4"));
            if (platformTaxRateEntity != null) {
                //税率
                entity.setTaxRate(platformTaxRateEntity.getTaxRate());
            }
            // 基础信息
            BeanUtils.copyProperties(entity, vo);
            vo.setApprovalId(entity.getId());
            list.add(vo);
        }
        return new PageResult<>(list, page.getTotal());
    }

    /**
     * 审批单 手动结算订单
     *
     * @param platformApprovalSettlementDto
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void manualSettlementOrder(PlatformApprovalSettlementDto platformApprovalSettlementDto) {
        //查询用户是否存在
        UserDto userDto = userServerClient.get(platformApprovalSettlementDto.getUserId());
        if (userDto == null) {
            throw new RpcCallException(new BusinessException(PlatformError.PLATFORM_USER_INFO_NOT_EXIST_ERROR));
        }
        //查询审批单是否存在
        PlatformApprovalEntity platformApprovalEntity = this.baseMapper.selectById(platformApprovalSettlementDto.getApprovalId());
        if (platformApprovalEntity == null) {
            throw new RequestBadException(PlatformError.PLATFORM_USER_APPROVAL_NOT_EXIST_ERROR);
        }
        //判断审批单状态
        if (platformApprovalEntity.getStatus() == 2) {
            throw new RequestBadException(PlatformError.PLATFORM_USER_APPROVAL_NOT_PASS_ERROR);
        }
        // 计算支付渠道金额总和是否一致
        BigDecimal payChannelsSum = platformApprovalSettlementDto.getPayChannels().stream()
                .map(PayCommand::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (payChannelsSum.compareTo(platformApprovalEntity.getTotalPrice()) != 0) {
            throw new RequestBadException(PlatformError.PLATFORM_USER_APPROVAL_AMOUNT_ERROR);
        }

        //查询平台信息
        List<PlatformInfoEntity> platformInfoEntities = platformInfoRepository.selectList(null);

        //订单结算
        PayOrderCommand payOrderCommand = new PayOrderCommand();
        payOrderCommand.setPayChannels(platformApprovalSettlementDto.getPayChannels());
        payOrderCommand.setOrderId(platformApprovalSettlementDto.getOrderId());
        payOrderCommand.setOrderDeductAmount(platformApprovalSettlementDto.getOrderDeductAmount());
        payOrderCommand.setOrderDeductReason(platformApprovalSettlementDto.getOrderDeductReason());
        //获取平台id
        platformInfoEntities.stream().findFirst().ifPresent(platformInfoEntity -> {
            payOrderCommand.setMerchantId(platformInfoEntity.getId());
            payOrderCommand.setUserPhone(platformInfoEntity.getPlatformPhone());
        });
        payOrderCommand.setCountryCode(userDto.getCountryCode());
        payOrderCommand.setUserPhone(userDto.getPhone());
        try {
            String result = orderClient.manualSettlementOrder(payOrderCommand);
            log.info("订单结算成功，订单ID：{}，结果：{}", payOrderCommand.getOrderId(), result);
        } catch (Exception e) {
            log.error("订单结算失败，订单ID：{}，错误信息：{}", payOrderCommand.getOrderId(), e.getMessage());
            throw new RuntimeException("订单结算失败", e);
        }
        //结算完成后更新审批单状态并且更新会员成为商户
        //更新审批单状态为
        platformApprovalEntity.setStatus(2);
        long time = DateUtil.offsetDay(DateUtil.date(), 365).getTime();
        platformApprovalEntity.setOverdueDate(time);
        platformApprovalEntity.setContractId(userDto.getContractId());
        //判断用户是否已经是商户,如果是商户就直接生成商户订阅信息
        if (userDto.getIdentity().equals(UserIdentity.Merchants.getValue())) {
            String tenantId = userDto.getMerchantId();
//                createMerchantSubscribe(platformApprovalEntity, userDtoResponseEntity, tenantId);
                //根据商户id查询商户信息`
                MerchantInfoEntity merchantInfoEntity = merchantInfoRepository.selectById(tenantId);
                //更新商户总存储量
//                BigDecimal bigInteger = BigDecimal.valueOf(platformApprovalEntity.getMemory() * 1024);
//                BigDecimal sumMemory = merchantInfoEntity.getSumMemory().add(bigInteger);
//                merchantInfoEntity.setSumMemory(sumMemory);
                baseMapper.updateById(platformApprovalEntity);
                merchantInfoRepository.update(merchantInfoEntity, new LambdaUpdateWrapper<MerchantInfoEntity>().eq(MerchantInfoEntity::getId, tenantId));
        } else {//如果不是商户就需要生成商户信息 代表第一次订阅数据
            //更新用户信息
            String registerPhone = userDto.getPhone();
            String countryCode = userDto.getCountryCode();
                MD5 md5 = new MD5();
                //生成租户id
                String tenantId = md5.digestHex(registerPhone);
                platformApprovalEntity.setTenantId(tenantId);
                baseMapper.updateById(platformApprovalEntity);
            int updateUser = userMapper.updateUserById(platformApprovalSettlementDto.getUserId(), UserIdentity.Merchants.getValue(), tenantId);
            if (updateUser > 0) {
                //根据租户ID查询merchant_info中是否有未删除记录
                LambdaQueryWrapper<MerchantInfoEntity> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(MerchantInfoEntity::getId, tenantId)
                        .eq(MerchantInfoEntity::getDeleted, 0);
                MerchantInfoEntity existingMerchant = merchantInfoRepository.selectOne(queryWrapper);
                if (existingMerchant != null) {
                    throw new RequestBadException(PlatformError.PLATFORM_MERCHANT_TENANTID_EXIST_ERROR);
                }
                //生成商户信息
                MerchantInfoEntity merchantInfoEntity = new MerchantInfoEntity();
                String userId = Objects.requireNonNull(userDto).getId();
                    merchantInfoEntity.setUserId(userId);
                    merchantInfoEntity.setId(tenantId);
                    merchantInfoEntity.setCountryCode(countryCode);
                    merchantInfoEntity.setRegisterPhone(registerPhone);
                    // 更新商户信息
                    merchantInfoEntity.setUsedMemory(BigDecimal.valueOf(0));
                    merchantInfoEntity.setOverdueDate(platformApprovalEntity.getOverdueDate());
                    merchantInfoEntity.setTaxNum(platformApprovalEntity.getTaxNum());
                    merchantInfoEntity.setEnterpriseEmail("-");
                    merchantInfoEntity.setLanguage("-");
                    merchantInfoEntity.setName("-");
                    merchantInfoEntity.setAddress("-");
                    merchantInfoEntity.setCurrency("-");
                    merchantInfoEntity.setPhone("-");
                    int insert = merchantInfoRepository.insert(merchantInfoEntity);
                    if (insert > 0) {
                        //新增一笔存储发放的记录
                        PlatformApprovalRecordEntity entity = new PlatformApprovalRecordEntity();
                        entity.setTenantId(tenantId);
                        long changeMemory = (long) platformApprovalEntity.getMemory() * 1024 * 1024;
                        entity.setChangeMemory(changeMemory );
                        entity.setDescription("开通商户");
                        entity.setApprovalId(platformApprovalSettlementDto.getApprovalId());
                        entity.setRecordType(ApprovalRecordType.SEND.getValue());
                        entity.setCreateTime(System.currentTimeMillis());
                        int insert1 = platformApprovalRecordRepository.insert(entity);
                        if (insert1 > 0) {
                            //调用通知接口-审批单审批完成通知商户获得发票提醒
                            SendNotificationCommand sendNotificationCommand = new SendNotificationCommand();
                            sendNotificationCommand.setType(NotificationType.NEW_APPROVAL);
                            //商户的userId
                            sendNotificationCommand.setReceiver(platformApprovalEntity.getUserId());
                            sendNotificationCommand.setTitle("审批单通过");
                            sendNotificationCommand.setContent("您的审批单通过了，发票已生成");
                            sendNotificationCommand.setData(null);
                            sendNotificationCommand.setTimestamp(System.currentTimeMillis());
                            try {
                                notificationServerClient.sendNotification(sendNotificationCommand);
                            } catch (Exception e) {
                                log.error("通知用户审批单通过失败", e);
                            }
                    }
                }
            }
        }
    }


    @Override
    public PlatformApprovalDto getApprovalByUserId(String id) {
        //根据用户id查询用户身份标识
        UserDto userDto = userMapper.getUserByUserId(id);
        if (userDto != null) {
            if (!userDto.getIdentity().equals(UserIdentity.Merchants.getValue())) {
                PlatformApprovalEntity entity = baseMapper.selectOne(new LambdaQueryWrapper<PlatformApprovalEntity>().eq(PlatformApprovalEntity::getUserId, id));
                if (entity != null) {
                    return convert.toDto(entity);
                } /*else {
                    throw new RequestBadException(PlatformError.PLATFORM_USER_APPROVAL_NOT_EXIST_ERROR);
                }*/
            } else {
                throw new RequestBadException(PlatformError.PLATFORM_USER_IS_MERCHANT_ERROR);

            }
        }
        throw new RequestBadException(PlatformError.PLATFORM_USER_INFO_NOT_EXIST_ERROR);
    }

    @Override
    public PlatformApprovalRecordVo counts() {
        /**
         * 1、商户总存储
         * 2、商户剩余存储
         * 3、图片占用存储--指的图片服务器空间存储
         * 4、发票--指的 发票数据库数据存储
         * 5、订单--指的 订单数据库数据存储
         * 6、商品--指的 商品数据库数据存储
         */
        String tenantId = ApplicationContext.getTenantId();
        log.info("counts...tenantId:{}", tenantId);
        //三元表达式
        //查询商户总存储商户id 总存储=sum(CHANGE_MEMORY) and recordType字段=SEND之和
        Long sumMemory = platformApprovalRecordRepository.selectList(new LambdaQueryWrapper<PlatformApprovalRecordEntity>()
                        .eq(PlatformApprovalRecordEntity::getRecordType, ApprovalRecordType.SEND.getValue())
                        .eq(PlatformApprovalRecordEntity::getTenantId,   tenantId==null? null :tenantId))
                .stream().map(item -> item.getChangeMemory()).reduce(0L, Long::sum);

        //查询商户剩余存储 商户id、recordType字段来进行计算 sum(CHANGE_MEMORY) and 剩余存储=SEND-OVER-USED
        Long usedMemory = platformApprovalRecordRepository.selectList(new LambdaQueryWrapper<PlatformApprovalRecordEntity>()
                        .eq(PlatformApprovalRecordEntity::getTenantId,  tenantId==null? null :tenantId)
                        .in(PlatformApprovalRecordEntity::getRecordType,
                                Arrays.asList(ApprovalRecordType.USED.getValue(), ApprovalRecordType.OVER.getValue())))
                .stream().map(item -> item.getChangeMemory()).reduce(0L, Long::sum);

        //剩余存储
        Long residueMemory = sumMemory - usedMemory;

        //3、数据来源 order:订单; invoice：发票; product:商品; cancel:取消; refund:退款; video:视频; img:图片;other：其他
        // 查询图片占用存储
        Long imgMemory = platformApprovalRecordRepository.selectList(new LambdaQueryWrapper<PlatformApprovalRecordEntity>()
                        .eq(PlatformApprovalRecordEntity::getDataSource, "img")
                        .eq(PlatformApprovalRecordEntity::getTenantId,  tenantId==null? null :tenantId))
                .stream().map(item -> item.getChangeMemory()).reduce(0L, Long::sum);

        // 查询视频占用存储
        Long videoMemory = platformApprovalRecordRepository.selectList(new LambdaQueryWrapper<PlatformApprovalRecordEntity>()
                        .eq(PlatformApprovalRecordEntity::getDataSource, "video")
                        .eq(PlatformApprovalRecordEntity::getTenantId,  tenantId==null? null :tenantId))
                .stream().map(item -> item.getChangeMemory()).reduce(0L, Long::sum);


        // 查询订单占用存储
        Long orderMemory = platformApprovalRecordRepository.selectList(new LambdaQueryWrapper<PlatformApprovalRecordEntity>()
                        .eq(PlatformApprovalRecordEntity::getDataSource, "order")
                        .eq(PlatformApprovalRecordEntity::getTenantId,  tenantId==null? null :tenantId))
                .stream().map(item -> item.getChangeMemory()).reduce(0L, Long::sum);

        // 查询发票占用存储
        Long invoiceMemory = platformApprovalRecordRepository.selectList(new LambdaQueryWrapper<PlatformApprovalRecordEntity>()
                        .eq(PlatformApprovalRecordEntity::getDataSource, "invoice")
                        .eq(PlatformApprovalRecordEntity::getTenantId, tenantId))
                .stream().map(item -> item.getChangeMemory()).reduce(0L, Long::sum);

        // 查询商品占用存储
        Long productMemory = platformApprovalRecordRepository.selectList(new LambdaQueryWrapper<PlatformApprovalRecordEntity>()
                        .eq(PlatformApprovalRecordEntity::getDataSource, "product")
                        .eq(PlatformApprovalRecordEntity::getTenantId, tenantId))
                .stream().map(item -> item.getChangeMemory()).reduce(0L, Long::sum);

        log.info("counts...sumMemory:{},usedMemory:{},residueMemory:{},imgMemory:{},videoMemory:{},orderMemory:{},invoiceMemory:{},productMemory:{}", sumMemory, usedMemory, residueMemory, imgMemory, videoMemory, orderMemory, invoiceMemory, productMemory);
        PlatformApprovalRecordVo vo = new PlatformApprovalRecordVo();
        vo.setSumMemory(sumMemory/1024/1024);
        vo.setUsedMemory(usedMemory/1024/1024);
        vo.setResidueMemory(residueMemory/1024/1024);
        vo.setImgMemory(imgMemory/1024/1024);
        vo.setVideoMemory(videoMemory/1024/1024);
        vo.setOrderMemory(orderMemory/1024/1024);
        vo.setInvoiceMemory(invoiceMemory/1024/1024);
        vo.setProductMemory(productMemory/1024/1024);
        return vo;
    }


    /**
     * 获取条件
     *
     * @param query
     * @return
     */
    private Wrapper<PlatformApprovalEntity> getWrapper(PlatformApprovalQuery query) {
        LambdaQueryWrapper<PlatformApprovalEntity> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isBlank(query.getOrder())){
            wrapper.orderByDesc(PlatformApprovalEntity::getStartTime);
        }
        if (StringUtils.isNotBlank(query.getTenantId())){
            wrapper.eq(PlatformApprovalEntity::getTenantId, query.getTenantId());
        }
//        if (StringUtils.isBlank(query.getStatus())){
//            wrapper.isNotNull(PlatformApprovalEntity::getTenantId);
//        }
        if (StringUtils.isNotBlank(query.getStatus())){
            wrapper.eq(PlatformApprovalEntity::getStatus, query.getStatus());
        }
        if (StringUtils.isNotBlank(query.getKeyword())) {
            wrapper.and(w ->
                    w.like(PlatformApprovalEntity::getOrderId, query.getKeyword())
                            .or()
                            .like(PlatformApprovalEntity::getUserId, query.getKeyword())

            );
        }
        // 添加时间范围条件
        if (query.getStartTime() != null && query.getEndTime() != null) {
            wrapper.between(PlatformApprovalEntity::getCreateTime, query.getStartTime(), query.getEndTime());
        } else if (query.getStartTime() != null) {
            wrapper.ge(PlatformApprovalEntity::getCreateTime, query.getStartTime());
        } else if (query.getEndTime() != null) {
            wrapper.le(PlatformApprovalEntity::getCreateTime, query.getEndTime());
        }
        return wrapper;
    }

    /**
     * 获取条件
     *
     * @param query
     * @return
     */
    private Wrapper<PlatformApprovalEntity> getIncomeWrapper(PlatformApprovalQuery query) {
        LambdaQueryWrapper<PlatformApprovalEntity> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isBlank(query.getOrder())){
            wrapper.orderByDesc(PlatformApprovalEntity::getStartTime);
        }
        if (StringUtils.isNotBlank(query.getTenantId())){
            wrapper.eq(PlatformApprovalEntity::getTenantId, query.getTenantId());
        }else {
            wrapper.isNotNull(PlatformApprovalEntity::getTenantId);
        }
        if (StringUtils.isNotBlank(query.getKeyword())) {
            wrapper.and(w ->
                    w.like(PlatformApprovalEntity::getOrderId, query.getKeyword())
                            .or()
                            .like(PlatformApprovalEntity::getUserId, query.getKeyword())

            );
        }
        // 添加时间范围条件
        if (query.getStartTime() != null && query.getEndTime() != null) {
            wrapper.between(PlatformApprovalEntity::getCreateTime, query.getStartTime(), query.getEndTime());
        } else if (query.getStartTime() != null) {
            wrapper.ge(PlatformApprovalEntity::getCreateTime, query.getStartTime());
        } else if (query.getEndTime() != null) {
            wrapper.le(PlatformApprovalEntity::getCreateTime, query.getEndTime());
        }
        return wrapper;
    }

}