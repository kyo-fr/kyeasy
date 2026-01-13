-- 商户
delete  from MERCHANT_ADVERTISED where  DELETED is not null;

delete  from MERCHANT_BANNER where  DELETED is not null;

delete  from MERCHANT_DISTRIBUTION where  DELETED is not null;

delete  from MERCHANT_FILE_UPLOAD where  DELETED is not null;

delete  from MERCHANT_FREIGHT where  DELETED is not null;

delete  from MERCHANT_HOLIDAY where  DELETED is not null;

delete  from MERCHANT_INFO where  DELETED is not null;

delete  from MERCHANT_KEY_WORDS where  DELETED is not null;

delete  from MERCHANT_MARKING where  DELETED is not null;

delete  from MERCHANT_OPENING_HOURS where  DELETED is not null;

delete  from MERCHANT_SIDE_BAR where  DELETED is not null;

delete  from MERCHANT_SPECIFICATION where  DELETED is not null;

delete  from MERCHANT_SUB_SPECIFICATION where  DELETED is not null;

delete  from MERCHANT_SUBSCRIBE where  DELETED is not null;

delete  from MERCHANT_WAREHOUSE where  DELETED is not null;

delete  from MERCHANT_WAREHOUSE where  DELETED is not null;

delete  from MERCHANT_WAREHOUSE_SEAT where  DELETED is not null;

-- 商品
delete  from PRODUCT_AUCTION where  DELETED is not null;

delete  from PRODUCT_AUCTION_PRICE_LOG where  DELETED is not null;

delete  from PRODUCT_BASE_INFO where  DELETED is not null;

delete  from PRODUCT_KEYWORDS where  DELETED is not null;

delete  from PRODUCT_LIST where  DELETED is not null;

delete  from PRODUCT_MARKING where  DELETED is not null;

delete  from PRODUCT_PREFERENTIAL where  DELETED is not null;

delete  from PRODUCT_SPECIFICATION where  DELETED is not null;

delete  from PRODUCT_SUB_SPECIFICATION where  DELETED is not null;

delete  from PRODUCT_TYPE where  DELETED is not null;

delete  from PRODUCT_WHOLESALE where  DELETED is not null;


-- 清理用户
delete from USERS where identity != 4;

-- 骑手
delete from RIDER;

-- 地址
delete from USER_ADDRESS;

-- 站内消息
delete from WS_NOTIFICATION_MESSAGE;

-- 订单
delete from ORDERS;

delete from  ORDER_ITEMS;

delete from ORDER_DELIVERY_INFO;

delete from ORDER_OPERATION_LOGS;

delete from ORDER_PAYMENTS RESTART;

delete from  ORDER_STATUS_LOGS;

delete from ORDER_ITEM_SPECIFICATIONS;

delete from ORDER_RESERVATION_INFO;

-- 会员中心
delete from MERCHANTS;

delete from ACCOUNTS;

delete from PAYMENT_ACCOUNT_FLOW;

delete from FEE_CONFIG RESTART;

delete from PAYMENT_ORDER;

delete from PAYMENT_TRANSACTIONS;

delete from WALLETS;
-- 发票
delete from PAY_INVOICE;

delete from PAY_INVOICE_ITEM;

delete from T_PARTY;

delete from PAY_ITEM;
