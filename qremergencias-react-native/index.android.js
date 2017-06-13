/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 * @flow
 */

import React, {Component} from 'react';
import {AppRegistry, Text, View, Button} from 'react-native';
import {StackNavigator} from 'react-navigation';
import BarcodeScanner from 'react-native-barcodescanner';

class ReaderScreen extends React.Component {
    static navigationOptions = {
        title: 'Scanner',
    };

    constructor(props) {
        super(props);

        this.state = {
            torchMode: 'off',
            cameraType: 'back',
        };
    }

    barcodeReceived(e) {
        console.log('Barcode: ' + e.data);
        console.log('Type: ' + e.type);
    }

    render() {
        return (
            <BarcodeScanner
                onBarCodeRead={this.barcodeReceived}
                style={{ flex: 1 }}
                torchMode={this.state.torchMode}
                cameraType={this.state.cameraType}
            />
        );
    }
}

class HomeScreen extends React.Component {
    static navigationOptions = {
        title: 'Welcome',
    };

    render() {
        const { navigate } = this.props.navigation;
        return (
            <View>
                <Text>Hello World!</Text>
                <Button
                    onPress={() => navigate('Reader')}
                    title="Scan QR"
                />
            </View>
        );
    }
}

const QREmergencias = StackNavigator({
    Home: { screen: HomeScreen },
    Reader: { screen: ReaderScreen },
});

AppRegistry.registerComponent('qremergencias', () => QREmergencias);
