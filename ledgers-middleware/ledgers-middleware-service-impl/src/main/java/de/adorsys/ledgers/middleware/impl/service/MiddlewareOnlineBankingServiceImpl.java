package de.adorsys.ledgers.middleware.impl.service;

import java.time.LocalDateTime;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.adorsys.ledgers.middleware.api.domain.sca.SCALoginResponseTO;
import de.adorsys.ledgers.middleware.api.domain.sca.ScaStatusTO;
import de.adorsys.ledgers.middleware.api.domain.um.AccessTokenTO;
import de.adorsys.ledgers.middleware.api.domain.um.BearerTokenTO;
import de.adorsys.ledgers.middleware.api.domain.um.LoginKeyDataTO;
import de.adorsys.ledgers.middleware.api.domain.um.ScaUserDataTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserRoleTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserTO;
import de.adorsys.ledgers.middleware.api.exception.AccountMiddlewareUncheckedException;
import de.adorsys.ledgers.middleware.api.exception.InsufficientPermissionMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.SCAMethodNotSupportedMiddleException;
import de.adorsys.ledgers.middleware.api.exception.SCAOperationExpiredMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.SCAOperationNotFoundMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.SCAOperationUsedOrStolenMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.SCAOperationValidationMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.UserAlreadyExistsMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.UserNotFoundMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.UserScaDataNotFoundMiddlewareException;
import de.adorsys.ledgers.middleware.api.service.MiddlewareOnlineBankingService;
import de.adorsys.ledgers.middleware.impl.converter.AccessTokenMapper;
import de.adorsys.ledgers.middleware.impl.converter.BearerTokenMapper;
import de.adorsys.ledgers.middleware.impl.converter.UserMapper;
import de.adorsys.ledgers.sca.domain.AuthCodeDataBO;
import de.adorsys.ledgers.sca.domain.OpTypeBO;
import de.adorsys.ledgers.sca.domain.SCAOperationBO;
import de.adorsys.ledgers.sca.domain.ScaStatusBO;
import de.adorsys.ledgers.sca.exception.SCAMethodNotSupportedException;
import de.adorsys.ledgers.sca.exception.SCAOperationExpiredException;
import de.adorsys.ledgers.sca.exception.SCAOperationNotFoundException;
import de.adorsys.ledgers.sca.exception.SCAOperationUsedOrStolenException;
import de.adorsys.ledgers.sca.exception.SCAOperationValidationException;
import de.adorsys.ledgers.sca.service.SCAOperationService;
import de.adorsys.ledgers.um.api.domain.BearerTokenBO;
import de.adorsys.ledgers.um.api.domain.UserBO;
import de.adorsys.ledgers.um.api.domain.UserRoleBO;
import de.adorsys.ledgers.um.api.exception.InsufficientPermissionException;
import de.adorsys.ledgers.um.api.exception.UserAlreadyExistsException;
import de.adorsys.ledgers.um.api.exception.UserNotFoundException;
import de.adorsys.ledgers.um.api.exception.UserScaDataNotFoundException;
import de.adorsys.ledgers.um.api.service.UserService;

@Service
@Transactional
public class MiddlewareOnlineBankingServiceImpl implements MiddlewareOnlineBankingService {
	private static final Logger logger = LoggerFactory.getLogger(MiddlewarePaymentServiceImpl.class);
	private final UserService userService;
	private final UserMapper userTOMapper;
	private final BearerTokenMapper bearerTokenMapper;
	private final AccessTokenMapper accessTokenMapper;
	private final SCAOperationService scaOperationService;
	private final SCAUtils scaUtils;
	private final AccessTokenTO accessTokenTO;
	private int defaultLoginTokenExpireInSeconds = 600; // 600 seconds.

	public MiddlewareOnlineBankingServiceImpl(UserService userService, UserMapper userTOMapper,
			BearerTokenMapper bearerTokenMapper, AccessTokenMapper accessTokenMapper,
			SCAOperationService scaOperationService, SCAUtils scaUtils, AccessTokenTO accessTokenTO) {
		super();
		this.userService = userService;
		this.userTOMapper = userTOMapper;
		this.bearerTokenMapper = bearerTokenMapper;
		this.accessTokenMapper = accessTokenMapper;
		this.scaOperationService = scaOperationService;
		this.scaUtils = scaUtils;
		this.accessTokenTO = accessTokenTO;
	}

	@Override
	@SuppressWarnings({"PMD.CyclomaticComplexity"})
	public SCALoginResponseTO authorise(String login, String pin, UserRoleTO role)
			throws UserNotFoundMiddlewareException, InsufficientPermissionMiddlewareException {
		try {
			UserRoleBO roleBo = UserRoleBO.valueOf(role.name());
			UserBO user = userService.findByLogin(login);
			LoginKeyDataTO keyData = new LoginKeyDataTO(user.getId(), LocalDateTime.now());
			String opId = keyData.toOpId();
			String scaId = keyData.toOpId();
			String opData = opId;
			String authorisationId = opId;
			// FOr login we use the login name and login time for authId and authorizationId.
			BearerTokenBO loginTokenBO = userService.authorise(login, pin, roleBo, scaId, scaId);
			if(loginTokenBO!=null) {
				if(!scaRequired(user, OpTypeBO.LOGIN)) {
					SCALoginResponseTO response = new SCALoginResponseTO();
					response.setScaStatus(ScaStatusTO.EXEMPTED);		
					BearerTokenBO scaTokenBO = userService.scaToken(loginTokenBO.getAccessTokenObject());
					response.setBearerToken(bearerTokenMapper.toBearerTokenTO(scaTokenBO));
					response.setScaId(scaTokenBO.getAccessTokenObject().getScaId());
					response.setExpiresInSeconds(scaTokenBO.getExpires_in());
					response.setStatusDate(LocalDateTime.now());
					return response;
				} else {
					SCAOperationBO scaOperationBO;
					UserTO userTo = scaUtils.user(user);
					String scaUserDataId = null;
					AuthCodeDataBO authCodeData = new AuthCodeDataBO(user.getLogin(), scaUserDataId , 
							scaId, opData, keyData.messageTemplate(), 
							defaultLoginTokenExpireInSeconds, OpTypeBO.LOGIN, authorisationId);
					if (userTo.getScaUserData().size() == 1) {
						ScaUserDataTO chosenScaMethod = userTo.getScaUserData().iterator().next();
						authCodeData.setScaUserDataId(chosenScaMethod.getId());
						try {
							scaOperationBO = scaOperationService.generateAuthCode(authCodeData, user, ScaStatusBO.SCAMETHODSELECTED);
						} catch (SCAMethodNotSupportedException | UserScaDataNotFoundException | SCAOperationValidationException
								| SCAOperationNotFoundException e) {
							throw new AccountMiddlewareUncheckedException(e.getMessage(), e);
						}
					} else {
						scaOperationBO = scaOperationService.createAuthCode(authCodeData, ScaStatusBO.PSUIDENTIFIED);
					}
					SCALoginResponseTO response = toScaResponse(user, keyData, scaOperationBO);
					userService.loginToken(loginTokenBO.getAccessTokenObject(), response.getAuthorisationId());
					response.setBearerToken(bearerTokenMapper.toBearerTokenTO(loginTokenBO));
					return response;
				}
			}
		} catch (UserNotFoundException e) {
			throw new UserNotFoundMiddlewareException(e.getMessage(), e);
		} catch (InsufficientPermissionException e) {
			throw new InsufficientPermissionMiddlewareException(e.getMessage(), e);
		}
		throw new InsufficientPermissionMiddlewareException("Unknown credentials.");
	}

	@Override
	public BearerTokenTO validate(String accessToken) throws UserNotFoundMiddlewareException, InsufficientPermissionMiddlewareException{
		try {
			return bearerTokenMapper.toBearerTokenTO(userService.validate(accessToken, new Date()));
		} catch (UserNotFoundException e) {
			throw new UserNotFoundMiddlewareException(e.getMessage(), e);
		} catch (InsufficientPermissionException e) {
			throw new InsufficientPermissionMiddlewareException(e.getMessage(), e);
		}
	}

	@Override
	public UserTO register(String login, String email, String pin, UserRoleTO role)
			throws UserAlreadyExistsMiddlewareException {
		UserTO user = new UserTO(login, email, pin);
		user.getUserRoles().add(role);
		UserBO userBO = userTOMapper.toUserBO(user);
		try {
			return userTOMapper.toUserTO(userService.create(userBO));
		} catch (UserAlreadyExistsException e) {
			throw new UserAlreadyExistsMiddlewareException(user, e);
		}
	}

	@Override
	@SuppressWarnings({"PMD.CyclomaticComplexity"})
	public SCALoginResponseTO generateLoginAuthCode(String scaUserDataId, String authorisationId, String userMessage,
			int validitySeconds) throws SCAOperationNotFoundMiddlewareException, InsufficientPermissionMiddlewareException, 
			SCAMethodNotSupportedMiddleException, UserScaDataNotFoundMiddlewareException, SCAOperationValidationMiddlewareException{
		try {
			UserBO user = scaUtils.userBO();
			SCAOperationBO scaOperationBO = scaOperationService.loadAuthCode(authorisationId);
			LoginKeyDataTO keyData = LoginKeyDataTO.fromOpId(scaOperationBO.getOpId());
			String opId = scaOperationBO.getOpId();
			String opData = opId;
			AuthCodeDataBO authCodeData = new AuthCodeDataBO(user.getLogin(), scaUserDataId, 
					opId, opData, userMessage, validitySeconds, 
					OpTypeBO.LOGIN, authorisationId);
			scaOperationBO = scaOperationService.generateAuthCode(authCodeData, user, ScaStatusBO.SCAMETHODSELECTED);
			SCALoginResponseTO scaResponse = toScaResponse(user, keyData, scaOperationBO);
			BearerTokenBO loginToken = userService.loginToken(accessTokenMapper.toAccessTokenBO(accessTokenTO), authorisationId);
			scaResponse.setBearerToken(bearerTokenMapper.toBearerTokenTO(loginToken));
			return scaResponse;
		} catch (SCAMethodNotSupportedException e) {
			throw new SCAMethodNotSupportedMiddleException(e);
		} catch (UserScaDataNotFoundException e) {
			throw new UserScaDataNotFoundMiddlewareException(e);
		} catch (SCAOperationNotFoundException e) {
			logger.error(e.getMessage(), e);
			throw new SCAOperationNotFoundMiddlewareException(e);
		} catch (SCAOperationValidationException e) {
			logger.error(e.getMessage(), e);
			throw new SCAOperationValidationMiddlewareException(e);
		} catch (InsufficientPermissionException e) {
			throw new InsufficientPermissionMiddlewareException(e.getMessage(), e);
		} catch (UserNotFoundException e) {
			throw new AccountMiddlewareUncheckedException(e.getMessage(), e);
		}
	}

	@Override
	@SuppressWarnings({"PMD.CyclomaticComplexity"})
	public SCALoginResponseTO authenticateForLogin(String authorisationId, String authCode)
			throws SCAOperationNotFoundMiddlewareException, SCAOperationValidationMiddlewareException,
			SCAOperationExpiredMiddlewareException, SCAOperationUsedOrStolenMiddlewareException, 
			InsufficientPermissionMiddlewareException {
		try {
			UserBO user = scaUtils.userBO();
			SCAOperationBO scaOperationBO = scaOperationService.loadAuthCode(authorisationId);
			LoginKeyDataTO keyData = LoginKeyDataTO.fromOpId(scaOperationBO.getOpId());
			boolean valid = scaOperationService.validateAuthCode(authorisationId, authorisationId, authorisationId, authCode);
			SCALoginResponseTO scaResponse = toScaResponse(user,keyData, scaOperationBO);
			if(valid) {
				BearerTokenBO scaToken = userService.scaToken(accessTokenMapper.toAccessTokenBO(accessTokenTO));
				scaResponse.setBearerToken(bearerTokenMapper.toBearerTokenTO(scaToken));
			}
			return scaResponse;
		} catch (SCAOperationNotFoundException e) {
			throw new SCAOperationNotFoundMiddlewareException(e);
		} catch (SCAOperationValidationException e) {
			throw new SCAOperationValidationMiddlewareException(e);
		} catch (SCAOperationExpiredException e) {
			throw new SCAOperationExpiredMiddlewareException(e);
		} catch (SCAOperationUsedOrStolenException e) {
			throw new SCAOperationUsedOrStolenMiddlewareException(e);
		} catch (InsufficientPermissionException e) {
			throw new InsufficientPermissionMiddlewareException(e.getMessage(), e);
		} catch (UserNotFoundException e) {
			throw new AccountMiddlewareUncheckedException(e.getMessage(), e);
		}
	}

	private SCALoginResponseTO toScaResponse(UserBO user, LoginKeyDataTO keyData,
			SCAOperationBO a) {
		SCALoginResponseTO response = new SCALoginResponseTO();
		UserTO userTO = scaUtils.user(user);
		response.setAuthorisationId(a.getId());
		response.setChosenScaMethod(scaUtils.getScaMethod(userTO, a.getScaMethodId()));
		response.setChallengeData(null);
		response.setExpiresInSeconds(a.getValiditySeconds());
		response.setScaId(a.getOpId());
		response.setPsuMessage(keyData.messageTemplate());
		response.setScaMethods(userTO.getScaUserData());
		response.setScaStatus(ScaStatusTO.valueOf(a.getScaStatus().name()));
		response.setStatusDate(a.getStatusTime());
		return response;
	}

	@SuppressWarnings("PMD.UnusedFormalParameter")
	private boolean scaRequired(UserBO user, OpTypeBO opType) {
		return scaUtils.hasSCA(user);
	}
	
}
