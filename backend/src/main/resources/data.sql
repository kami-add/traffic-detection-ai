INSERT IGNORE INTO risk_rule(rule_key, weight, description) VALUES
('URL_TOO_LONG', 18, 'URL长度超过80字符'),
('IP_AS_DOMAIN', 20, '使用IP地址伪装域名'),
('SUSPICIOUS_KEYWORDS', 20, '命中敏感关键词');
